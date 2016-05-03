package com.micabytes.gui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.micabytes.R;
import com.micabytes.util.StringHandler;

import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class FileNavigationLayout extends LinearLayout implements AdapterView.OnItemClickListener {
  private final Context context;
  private FileItemListener itemListener;
  private final TextView pathLabel;
  private final ListView listView;
  private List<String> path;
  @NonNls private String root = "/";

  public FileNavigationLayout(Context ctx, AttributeSet attrs) {
    super(ctx, attrs);
    context = ctx;
    LayoutInflater layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    layoutInflater.inflate(R.layout.view_file_navigation, this);
    pathLabel = (TextView) findViewById(R.id.FileNavigationPath);
    listView = (ListView) findViewById(R.id.FileNavigationList);
    setFolder(root);
  }

  public void setRoot(String rootDir) {
    root = rootDir;
  }

  public void setFileItemListener(FileItemListener l) {
    itemListener = l;
  }

  public void setFolder(String dirPath) {
    pathLabel.setText(StringHandler.get(R.string.filenavigation_location, dirPath));
    List<String> item = new ArrayList<>();
    path = new ArrayList<>();
    File f = new File(dirPath);
    File[] files = f.listFiles();
    if (!dirPath.equals(root)) {
      item.add(root);
      path.add(root);
      item.add("../");
      path.add(f.getParent());
    }
    for (File file : files) {
      path.add(file.getPath());
      if (file.isDirectory())
        item.add(file.getName() + StringHandler.SLASH);
      else
        item.add(file.getName());
    }
    setItemList(item);
  }

  public void setItemList(List<String> item){
    ArrayAdapter<String> fileList = new ArrayAdapter<>(context, R.layout.entry_file_navigation, item);
    listView.setAdapter(fileList);
    listView.setOnItemClickListener(this);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    File file = new File(path.get(position));
    if (file.isDirectory()) {
      if (file.canRead())
        setFolder(path.get(position));
      else {
        if (itemListener != null) {
          itemListener.onFileInaccessible(file);
        }
      }
    } else {
      if (itemListener != null) {
        itemListener.onFileClicked(file);
      }
    }
  }


}
