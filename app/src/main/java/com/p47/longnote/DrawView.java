 package com.p47.longnote;

        import android.content.Context;
        import android.graphics.Bitmap;
        import android.graphics.Canvas;
        import android.graphics.Color;
        import android.graphics.Paint;
        import android.graphics.Path;
        import android.graphics.Point;
        import android.os.Bundle;
        import android.os.Parcel;
        import android.os.Parcelable;
        import android.util.AttributeSet;
        import android.util.Log;
        import android.view.MotionEvent;
        import android.view.View;

        import java.io.Serializable;
        import java.util.ArrayList;

 public class DrawView extends View {

     private int bgColor = Color.WHITE;
     private Path path = new Path();
     private Bitmap canvasBitmap;
     private Canvas canvas;
     private final int numColors = 3;
     private ArrayList<Path>[] paths = new ArrayList[numColors];
     //ind 0 = black, ind 1 = blue
     private final int colorList[] = { Color.BLACK, bgColor, bgColor };
     private int currentColor = 0; //default is black
     private ArrayList<Path> undonePaths = new ArrayList<>();
     private ArrayList<Integer> undoneColors = new ArrayList<>();
     private Paint paints[] = new Paint[numColors];
     private ArrayList<Integer> strokeOrderColor = new ArrayList<>();
     private ArrayList<Integer> strokeOrderPath = new ArrayList<>();
     private int numStrokes = 0;

     private int numUndo = 0;
     private final int eraserInd = 1;
     private final int clearInd = 2;

//TODO: Crashes when closing
     public DrawView(Context context, AttributeSet attrs) {
         super(context, attrs);
         final int defaultWidth = 15;
         final int eraserWidth = 50;

         //initialize paint list
         for (int i = 0; i < numColors; i++){
             paints[i] = new Paint();
             paints[i].setAntiAlias(true);
             if (i == clearInd){ paints[i].setStyle(Paint.Style.FILL); }
             else { paints[i].setStyle(Paint.Style.STROKE); }
             paints[i].setStrokeJoin(Paint.Join.ROUND);
             if (i == eraserInd){ paints[i].setStrokeWidth(eraserWidth); }
             else {paints[i].setStrokeWidth(defaultWidth);}
             paints[i].setColor(colorList[i]);
         }

         //initialize paths
         for (int j = 0; j < numColors; j++){
             paths[j] = new ArrayList<>();
         }

     }

     public void setColor(int color) {
          currentColor = color;
     }

     //Draws rectangle
     public void clearCanvas() {

         int scrWidth = canvas.getWidth();
         int scrHeight = canvas.getHeight();

         canvas.drawRect(0, 0, scrWidth, scrHeight, paints[getClearInd()]);

         //add to path list
         Path newPath = new Path(path);
         newPath.addRect(0, 0, scrWidth, scrHeight, Path.Direction.CW);
         paths[getClearInd()].add(newPath);
         strokeOrderColor.add(getClearInd());
         strokeOrderPath.add(paths[getClearInd()].size()-1);
         numStrokes++;

         path.reset();

         invalidate();
     }

     public void undo() {
         if (numStrokes > 0) {

             int lastPColorInd = strokeOrderColor.get(strokeOrderColor.size()-1);
             int lastPPathInd = strokeOrderPath.get(strokeOrderPath.size()-1);

             //remove from current paths and add to list of undone paths
             undoneColors.add(lastPColorInd);
             undonePaths.add(paths[lastPColorInd].remove(lastPPathInd));

             strokeOrderColor.remove(strokeOrderColor.size()-1);
             strokeOrderPath.remove(strokeOrderPath.size()-1);

             //bookkeeping
             numStrokes--;
             numUndo++;
             invalidate();
         }
     }
     public void redo() {
         if (numUndo > 0) {
             int lastUndoneColor = undoneColors.get(undoneColors.size()-1);
             int lastUndonePathLocation = undonePaths.size()-1;

             //add to current paths and remove from list of undone paths
             paths[lastUndoneColor].add(undonePaths.remove(lastUndonePathLocation) );
             undoneColors.remove(undoneColors.size()-1);

             strokeOrderColor.add(lastUndoneColor);
             strokeOrderPath.add(paths[lastUndoneColor].size()-1);

             //bookkeeping
             numStrokes++;
             numUndo--;
             invalidate();
         }
     }

     public Bitmap getDrawingBitmap() {
         return canvasBitmap;
     }


     @Override
     protected void onSizeChanged(int w, int h, int oldw, int oldh) {
         super.onSizeChanged(w, h, oldw, oldh);
         canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
         canvas = new Canvas(canvasBitmap);
         canvas.drawColor(bgColor);
         invalidate();
     }



     @Override
     protected void onDraw(Canvas canvas) {
         super.onDraw(canvas);

         for (int i = 0; i < numStrokes; i++){
             canvas.drawPath(paths[strokeOrderColor.get(i)].get(strokeOrderPath.get(i)), paints[strokeOrderColor.get(i)]);
         }

         canvas.drawPath(path,paints[currentColor]);

     }

     @Override
     public boolean onTouchEvent(MotionEvent event) {
         float x = event.getX();
         float y = event.getY();

         switch (event.getAction()) {


             case MotionEvent.ACTION_DOWN:
                 path.moveTo(x, y);
                 return true;

             case MotionEvent.ACTION_MOVE:
                 path.lineTo(x, y);
                 break;

             case MotionEvent.ACTION_UP:
                 Path newPath = new Path(path);
                 paths[currentColor].add(newPath);
                 strokeOrderColor.add(currentColor);
                 strokeOrderPath.add(paths[currentColor].size()-1);
                 numStrokes++;
                 path.reset();
                 path.moveTo(x, y);

                 break;

             default:
                 return false;
         }

         invalidate();
         return true;
     }

     //getters
    public int getCurrentColor(){
         return currentColor;
    }
    public int getEraserInd(){
         return eraserInd;
    }
     public int getClearInd(){
         return clearInd;
     }


     //saving instance
/*
     @Override
     public Parcelable onSaveInstanceState() {
         Bundle bundle = new Bundle();
         bundle.putParcelable("superState", super.onSaveInstanceState());
         saveDrawingState(bundle);
         return bundle;
     }


     private void saveDrawingState(Bundle bundle) {

         //ArrayList<Parcelable>savedPaths[] = new ArrayList[numColors];
         ArrayList<Path>[] savedPaths = paths.clone();

         ArrayList<Parcelable>savedUndonePaths = new ArrayList();

         //ArrayList savedUndonePaths = (ArrayList) undonePaths.clone();

        // for (int i = 0; i < numColors; i++){
        //     for (int j = 0; j < paths[i].size(); j++){
        //         savedPaths[i].add((Parcelable) paths[i].get(j));
        //     }
         //}


         for (int k = 0; k < numUndo; k++){
             savedUndonePaths.add((Parcelable)undonePaths.get(k));
         }

         int savedNumStrokes = numStrokes;
         int savedNumUndone = numUndo;
         int savedCurrentColor = currentColor;
         ArrayList savedStrokeOrderColor = (ArrayList)strokeOrderColor.clone();
         ArrayList savedStrokeOrderPath = (ArrayList)strokeOrderPath.clone();
         ArrayList savedUndoneColors = (ArrayList)undoneColors.clone();


         //bundle.putParcelableArray("savedPaths", savedPaths);

         bundle.putSerializable("savedPaths", savedPaths);

         bundle.putParcelableArrayList("savedUndonePaths", savedUndonePaths);



         //bundle.putSerializable("savedPaths", savedPaths);
         //bundle.putSerializable("savedUndonePaths", savedUndonePaths);

         bundle.putIntegerArrayList("savedStrokeOrderColor", savedStrokeOrderColor);
         bundle.putIntegerArrayList("savedStrokeOrderPath", savedStrokeOrderPath);
         bundle.putIntegerArrayList("savedUndoneColors", savedUndoneColors);
         bundle.putInt("savedNumStrokes", savedNumStrokes);
         bundle.putInt("savedNumUndone", savedNumUndone);
         bundle.putInt("currentColor", savedCurrentColor);
     }

     @Override
     public void onRestoreInstanceState(Parcelable state) {
         if (state instanceof Bundle) {
             Bundle bundle = (Bundle) state;
             restoreDrawingState(bundle);
             state = bundle.getParcelable("superState");
         }
         super.onRestoreInstanceState(state);
     }

     private void restoreDrawingState(Bundle bundle) {
         ArrayList<Parcelable> savedPaths;
         ArrayList<Parcelable> savedUndonePaths;
         ArrayList<Integer> savedUndoneColors;
         ArrayList<Integer> savedStrokeOrderColor;
         ArrayList<Integer> savedStrokeOrderPath;
         int savedNumStrokes;
         int savedNumUndone;
         int savedCurrentColor;
         try {
             //savedPaths = (ArrayList<Path>[]) bundle.getSerializable("savedPaths");
             //savedPaths = bundle.getParcelableArrayList("saved paths");
             //savedUndonePaths = bundle.get
             savedUndonePaths = bundle.getParcelableArrayList("savedUndonePaths");
             savedUndoneColors = bundle.getIntegerArrayList("savedUndoneColors");
             savedStrokeOrderColor = bundle.getIntegerArrayList("savedStrokeOrderColor");
             savedStrokeOrderPath = bundle.getIntegerArrayList("savedStrokeOrderPath");
             savedNumStrokes = bundle.getInt("savedNumStrokes");
             savedNumUndone = bundle.getInt("savedNumUndone");
             savedCurrentColor = bundle.getInt("savedCurrentColor");

             //paths = savedPaths.clone();

             for (int i = 0; i < numUndo; i++){
                 undonePaths.add((Path)savedUndonePaths.get(i));
                 undoneColors.add(savedUndoneColors.get(i));
             }
             currentColor = savedCurrentColor;


             numStrokes = savedNumStrokes;
             numUndo = savedNumUndone;

             undonePaths.clear();
             undoneColors.clear();


             strokeOrderColor.clear();
             strokeOrderPath.clear();


             for (int i = 0; i < numStrokes; i++) {
                 strokeOrderColor.add(savedStrokeOrderColor.get(i));
                 strokeOrderPath.add(savedStrokeOrderPath.get(i));
             }
         }
         catch (Exception BadParcelableException){
            Log.e("BadParcelableException","Fatal error in restoreDrawingState");
         }


     }*/

 }
