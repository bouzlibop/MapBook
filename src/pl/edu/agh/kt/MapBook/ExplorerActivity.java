package pl.edu.agh.kt.MapBook;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;

/**
 * Created with IntelliJ IDEA.
 * User: adba
 * Date: 03.12.13
 */
public class ExplorerActivity extends Activity {

  private static int RESULT_LOAD_IMAGE = 1;
  private static boolean CAN_DRAW_POINT = false;
  ImageView mapView;
  String picturePath;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.explorer);
    mapView = (ImageView) findViewById(R.id.mapView);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu){
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.explorer_activity_actions, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item){
    switch(item.getItemId()){
      case R.id.action_new_map:
        loadNewMap();
        return true;
      case R.id.action_save_map:
        Log.w("Action", "SAVE MAP");
        return true;
      case R.id.action_new_point:
        enableDrawing();
        return true;
      case R.id.action_show_pos:
        Log.w("Action", "SHOW POSITION");
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void loadNewMap() {
    Intent intent = new Intent( Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    startActivityForResult(intent, RESULT_LOAD_IMAGE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
      Uri selectedImage = data.getData();
      String[] filePathColumn = { MediaStore.Images.Media.DATA };

      Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
      cursor.moveToFirst();

      int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
      picturePath = cursor.getString(columnIndex);
      cursor.close();

      mapView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

      mapView.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
          if(CAN_DRAW_POINT){
            Log.i("Drawing new Point at:","x="+String.valueOf(event.getX())+", y="+String.valueOf(event.getY()));
            drawPoint(event.getX(), event.getY());
          }
          return true;
        }
      });
    }
  }

  private void enableDrawing() {
    CAN_DRAW_POINT = true;
  }

  private void drawPoint(float x, float y) {

    Bitmap myBitmap = BitmapFactory.decodeFile(picturePath);
    Paint myPosPaint = new Paint();
    myPosPaint.setColor(Color.rgb(0, 0, 255));
    myPosPaint.setStrokeWidth(5);

    Bitmap tempBitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(), Bitmap.Config.RGB_565);
    Canvas tempCanvas = new Canvas(tempBitmap);
    tempCanvas.drawBitmap(myBitmap, 0, 0, null);
    tempCanvas.drawRoundRect(new RectF(x-5,y-5,x+5,y+5),2, 2, myPosPaint);
    mapView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
    CAN_DRAW_POINT = false;
  }

}