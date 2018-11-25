package it.drakefk.metar.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import it.drakefk.metar.R;

public class FolderSearch extends ListActivity {

    public static final String TAG = "FolderSearch";

    public static final String UP_FOLDER_NAME = "..";
    public static final String FILE_SELECTED = "fileSelected";
    public static final String FILTER_EXTENSION = "filter extension";
    public static final String FILTER_CONTAINS_NAME = "filter contains name";

    private File currentDir;
    private ArrayAdapter<FileObject> adapter;
    private ArrayList<String> extensions;
    private ArrayList<String> contains;

    /** File Returned to the Activity*/
    private File fileSelected;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getStringArrayList(FILTER_EXTENSION) != null) {
                this.extensions = extras.getStringArrayList(FILTER_EXTENSION);
            }
            if (extras.getStringArrayList(FILTER_CONTAINS_NAME) != null) {
                this.contains = extras.getStringArrayList(FILTER_CONTAINS_NAME);
            }
        }

        currentDir = Environment.getExternalStorageDirectory();
        drawList(currentDir);
    }

    public static Intent createIntent(Context context) {
        /* Import new datasets of Vuforia*/
        Intent intent = new Intent(context, FolderSearch.class);
        ArrayList<String> extensions = new ArrayList<String>();
        extensions.add(".xml"); //can be used for multiple filters
        extensions.add(".dat");
        extensions.add(".zip");
        intent.putStringArrayListExtra(FolderSearch.FILTER_EXTENSION, extensions);
        return intent;
    }

    private void drawList(File f) {
        File[] dirs = null;

        dirs = f.listFiles();

        this.setTitle(/*getString(android.R.string.currentDir) +*/ ": " + f.getName());
        List<FileObject> dir = new ArrayList<FileObject>();
        List<FileObject> fls = new ArrayList<FileObject>();
        try {
            for (File ff : dirs) {
                if (ff.isDirectory() ){
                    dir.add(new FileObject(ff.getName(), ff
                            .getAbsolutePath(), true, ff.list().length));
                }
                else {
                    // Use only the files with the selected extension
                    if( this.extensions != null && !this.extensions.isEmpty()){
                        if( this.contains != null && !this.contains.isEmpty()){
                            // Extension and Contain
                            for( String ext : this.extensions){
                                if(ff.getName().endsWith(ext)){
                                    for( String name : this.contains){
                                        if(ff.getName().contains(name)){
                                            fls.add(new FileObject(ff.getName(), ff.getAbsolutePath(), false, ff.length()));
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        else{
                            // Extension Only
                            for( String ext : this.extensions){
                                if(ff.getName().endsWith(ext)){
                                    fls.add(new FileObject(ff.getName(), ff.getAbsolutePath(), false, ff.length()));
                                    break;
                                }
                            }
                        }
                    }
                    else{
                        if( this.contains != null && !this.contains.isEmpty()){
                            // Contain Only
                            for( String name : this.contains){
                                if(ff.getName().contains(name)){
                                    fls.add(new FileObject(ff.getName(), ff.getAbsolutePath(), false, ff.length()));
                                    break;
                                }
                            }
                        }
                        else{
                            // No Extension No Contain
                            fls.add(new FileObject(ff.getName(), ff.getAbsolutePath(), false, ff.length()));
                        }
                    }
                }
            }
        } catch (Exception e) {

        }
        Collections.sort(dir);
        Collections.sort(fls);
        dir.addAll(fls);
        if (!f.getName().equalsIgnoreCase(Environment.getExternalStorageDirectory().getName()/*"sdcard"*/)) {
            if (f.getParentFile() != null){
                dir.add(0, new FileObject(UP_FOLDER_NAME, f.getParent(), true, f.length()));
            }
        }
        adapter = new ArrayAdapterFile(FolderSearch.this, R.layout.file_item,
                dir);
        this.setListAdapter(adapter);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((!currentDir.getName().equals("sdcard")) && (currentDir.getParentFile() != null)) {
                currentDir = currentDir.getParentFile();
                drawList(currentDir);
            } else {
                finish();
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        FileObject o = adapter.getItem(position);
        if (o.isFolder() ) {
            currentDir = new File(o.getPath());
            drawList(currentDir);
        } else {
            fileSelected = new File(o.getPath());
            Intent intent = new Intent();
            intent.putExtra(FILE_SELECTED, fileSelected.getAbsolutePath());
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    private class FileObject implements Comparable<FileObject>{
        private String name;
        private String path;
        private boolean folder;
        private long dimensionByte;

        /**
         * @param name
         * @param path
         * @param folder
         */
        public FileObject(String name, String path, boolean folder) {
            this.name = name;
            this.path = path;
            this.folder = folder;
        }

        public FileObject(String name, String path, boolean folder,
                          long dimensionByte) {
            this(name, path, folder);
            this.dimensionByte = dimensionByte;
        }

        @Override
        public int compareTo(FileObject f) {
            if(this.name != null)
                return this.name.toLowerCase().compareTo(f.getName().toLowerCase());
            else
                throw new IllegalArgumentException();
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the path
         */
        public String getPath() {
            return path;
        }

        /**
         * @return the folder
         */
        public boolean isFolder() {
            return folder;
        }

        /**
         * @return the dimensionByte
         */
        public long getDimensionByte() {
            return dimensionByte;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @param path the path to set
         */
        public void setPath(String path) {
            this.path = path;
        }

        /**
         * @param folder the folder to set
         */
        public void setFolder(boolean folder) {
            this.folder = folder;
        }

        /**
         * @param dimensionByte the dimensionByte to set
         */
        public void setDimensionByte(long dimensionByte) {
            this.dimensionByte = dimensionByte;
        }
    }

    public class ArrayAdapterFile extends ArrayAdapter<FileObject> {

        private Context c;
        private int id;
        private List<FileObject> items;

        public ArrayAdapterFile(Context context, int textViewResourceId,
                                List<FileObject> objects) {
            super(context, textViewResourceId, objects);
            c = context;
            id = textViewResourceId;
            items = objects;
        }

        public FileObject getItem(int i) {
            return items.get(i);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) c
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(id, null);
            }
            final FileObject fo = items.get(position);
            if (fo != null) {
                ImageView im = (ImageView) v.findViewById(R.id.file_item_img);
                TextView t1 = (TextView) v.findViewById(R.id.file_item_txt_name);
                TextView t2 = (TextView) v.findViewById(R.id.file_item_txt_info);

                if(fo.isFolder()){
                    im.setImageResource(R.drawable.icon_folder);
//				} else if (fo.getData().equalsIgnoreCase("parent directory")) {
//					im.setImageResource(R.drawable.back32);
                } else {
                    String name = fo.getName().toLowerCase();
                    if (name.endsWith(".xls") ||  name.endsWith(".xlsx")){
                        im.setImageResource(R.drawable.icon_document);}
                    else if (name.endsWith(".doc") ||  name.endsWith(".docx")){
                        im.setImageResource(R.drawable.icon_document);}
                    else if (name.endsWith(".ppt") ||  fo.getName().endsWith(".pptx")){
                        im.setImageResource(R.drawable.icon_document);}
                    else if (name.endsWith(".pdf")){
                        im.setImageResource(R.drawable.icon_document);}
                    else if (name.endsWith(".apk")){
                        im.setImageResource(R.drawable.icon_file);}
                    else if (name.endsWith(".txt")){
                        im.setImageResource(R.drawable.icon_document);}
                    else if (name.endsWith(".xml")){
                        im.setImageResource(R.drawable.icon_xml);}
                    else if (name.endsWith(".jpg") || name.endsWith(".jpeg")){
                        im.setImageResource(R.drawable.icon_image);}
                    else if (name.endsWith(".png")){im.setImageResource(R.drawable.icon_image);}
                    else if (name.endsWith(".zip")){im.setImageResource(R.drawable.icon_zip);}
                    else if (name.endsWith(".rtf")){im.setImageResource(R.drawable.icon_document);}
                    else if (name.endsWith(".gif")){im.setImageResource(R.drawable.icon_image);}
                    else{
                        im.setImageResource(R.drawable.icon_file);
                    }
                }

                if (t1 != null){
                    t1.setText(fo.getName());
                }

                if( t2 != null ){
                    if( fo.isFolder() && !fo.getName().equals(UP_FOLDER_NAME)){
                        // For the folder use the number of files it holds
                        t2.setText(fo.getDimensionByte()+" Files");
                    }
                    else if( !fo.isFolder() && fo.getDimensionByte() >= 0){
                        // For the files use the dimension in KBytes
                        t2.setText(Math.round(fo.getDimensionByte()*100d/1024)*0.01d+" KB");
                    }
                }

            }
            return v;
        }

    }
}
