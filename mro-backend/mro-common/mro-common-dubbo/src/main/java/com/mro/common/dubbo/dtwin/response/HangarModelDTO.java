package com.mro.common.dubbo.dtwin.response;

import java.io.Serializable;

public record HangarModelDTO(Long id, String name, String modelUrl, String version) implements Serializable {}
