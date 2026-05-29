<template>
  <div ref="canvasRef" class="hangar-scene" />
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'
import { DRACOLoader } from 'three/examples/jsm/loaders/DRACOLoader.js'

const props = defineProps({
  modelUrl: { type: String, default: null },
  workstations: { type: Array, default: () => [] }
})

const emit = defineEmits(['workstation-click'])

const canvasRef = ref(null)

// Status → colour mapping
const STATUS_COLOR = {
  occupied:    0xff6b35,
  idle:        0x52c41a,
  maintenance: 0xfaad14,
  default:     0x8c8c8c
}

let renderer, scene, camera, controls, animId
const wsMeshMap = {}   // workstationId → Mesh

function colorFor(status) {
  return STATUS_COLOR[status] ?? STATUS_COLOR.default
}

function init() {
  const el = canvasRef.value
  renderer = new THREE.WebGLRenderer({ antialias: true })
  renderer.setPixelRatio(window.devicePixelRatio)
  renderer.setSize(el.clientWidth, el.clientHeight)
  renderer.shadowMap.enabled = true
  el.appendChild(renderer.domElement)

  scene = new THREE.Scene()
  scene.background = new THREE.Color(0x0d1117)
  scene.fog = new THREE.Fog(0x0d1117, 80, 200)

  camera = new THREE.PerspectiveCamera(45, el.clientWidth / el.clientHeight, 0.1, 500)
  camera.position.set(0, 60, 100)

  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.05
  controls.maxPolarAngle = Math.PI / 2.2

  // Lighting
  const ambient = new THREE.AmbientLight(0xffffff, 0.6)
  scene.add(ambient)
  const dir = new THREE.DirectionalLight(0xffffff, 1.2)
  dir.position.set(40, 80, 40)
  dir.castShadow = true
  scene.add(dir)

  // Floor grid
  const grid = new THREE.GridHelper(120, 24, 0x1a2332, 0x1a2332)
  scene.add(grid)

  // Floor plane
  const floor = new THREE.Mesh(
    new THREE.PlaneGeometry(120, 120),
    new THREE.MeshStandardMaterial({ color: 0x111820, roughness: 0.9 })
  )
  floor.rotation.x = -Math.PI / 2
  floor.receiveShadow = true
  scene.add(floor)

  // Click picking
  renderer.domElement.addEventListener('click', onCanvasClick)

  // Resize observer
  const ro = new ResizeObserver(() => onResize())
  ro.observe(el)

  animate()
}

function buildWorkstationMeshes(workstations) {
  // Remove stale meshes
  Object.values(wsMeshMap).forEach(m => scene.remove(m))
  Object.keys(wsMeshMap).forEach(k => delete wsMeshMap[k])

  workstations.forEach(ws => {
    const geo = new THREE.BoxGeometry(8, 0.4, 12)
    const mat = new THREE.MeshStandardMaterial({
      color: colorFor(ws.status),
      roughness: 0.4,
      metalness: 0.3,
      emissive: colorFor(ws.status),
      emissiveIntensity: 0.15
    })
    const mesh = new THREE.Mesh(geo, mat)
    mesh.castShadow = true
    mesh.receiveShadow = true
    // Map backend coords (x,z centred on scene, y=0 floor)
    mesh.position.set(
      (ws.positionX ?? 0) - 35,
      0.2,
      (ws.positionZ ?? 0) - 45
    )
    mesh.userData = { workstationId: ws.id, ws }
    scene.add(mesh)
    wsMeshMap[ws.id] = mesh

    // Label sprite
    addLabel(mesh.position.clone().setY(1.5), ws.name)
  })
}

function addLabel(position, text) {
  const canvas = document.createElement('canvas')
  canvas.width = 256; canvas.height = 48
  const ctx = canvas.getContext('2d')
  ctx.fillStyle = 'rgba(0,0,0,0.6)'
  ctx.roundRect?.(0, 0, 256, 48, 6) ?? ctx.fillRect(0, 0, 256, 48)
  ctx.fill()
  ctx.fillStyle = '#ffffff'
  ctx.font = '20px sans-serif'
  ctx.textAlign = 'center'
  ctx.fillText(text, 128, 32)
  const tex = new THREE.CanvasTexture(canvas)
  const sprite = new THREE.Sprite(new THREE.SpriteMaterial({ map: tex, transparent: true }))
  sprite.position.copy(position)
  sprite.scale.set(8, 1.5, 1)
  scene.add(sprite)
}

function loadGltfModel(url) {
  const dracoLoader = new DRACOLoader()
  dracoLoader.setDecoderPath('https://www.gstatic.com/draco/versioned/decoders/1.5.7/')
  const loader = new GLTFLoader()
  loader.setDRACOLoader(dracoLoader)
  loader.load(url, (gltf) => {
    gltf.scene.traverse(child => {
      if (child.isMesh) {
        child.castShadow = true
        child.receiveShadow = true
      }
    })
    scene.add(gltf.scene)
  }, undefined, (err) => {
    console.warn('glTF load failed, using procedural scene', err)
  })
}

const raycaster = new THREE.Raycaster()
const pointer = new THREE.Vector2()

function onCanvasClick(event) {
  const rect = renderer.domElement.getBoundingClientRect()
  pointer.x =  ((event.clientX - rect.left) / rect.width)  * 2 - 1
  pointer.y = -((event.clientY - rect.top)  / rect.height) * 2 + 1
  raycaster.setFromCamera(pointer, camera)
  const hits = raycaster.intersectObjects(Object.values(wsMeshMap))
  if (hits.length > 0) {
    emit('workstation-click', hits[0].object.userData.ws)
  }
}

function onResize() {
  const el = canvasRef.value
  if (!el || !renderer) return
  camera.aspect = el.clientWidth / el.clientHeight
  camera.updateProjectionMatrix()
  renderer.setSize(el.clientWidth, el.clientHeight)
}

function animate() {
  animId = requestAnimationFrame(animate)
  controls.update()
  renderer.render(scene, camera)
}

// Public: update a single workstation colour in real-time
function updateWorkstationStatus(workstationId, status) {
  const mesh = wsMeshMap[workstationId]
  if (!mesh) return
  const c = colorFor(status)
  mesh.material.color.setHex(c)
  mesh.material.emissive.setHex(c)
}

defineExpose({ updateWorkstationStatus })

onMounted(() => {
  init()
  if (props.workstations.length) buildWorkstationMeshes(props.workstations)
  if (props.modelUrl) loadGltfModel(props.modelUrl)
})

watch(() => props.workstations, (ws) => {
  if (ws.length && scene) buildWorkstationMeshes(ws)
}, { deep: true })

watch(() => props.modelUrl, (url) => {
  if (url && scene) loadGltfModel(url)
})

onUnmounted(() => {
  cancelAnimationFrame(animId)
  renderer?.dispose()
})
</script>

<style scoped>
.hangar-scene {
  width: 100%;
  height: 100%;
  min-height: 480px;
  overflow: hidden;
  border-radius: 4px;
}
</style>
