package com.micabytes.gui;

import java.io.File;

public interface FileItemListener {
  // File is inaccessible
  void onFileInaccessible(File file);
  // File is pressed/clicked
  void onFileClicked(File file);
}
