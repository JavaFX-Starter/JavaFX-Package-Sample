package com.icuxika.model;

import java.util.List;

public record UpdateIndex(String version, List<FileInfo> files) {
}
