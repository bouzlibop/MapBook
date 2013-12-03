package pl.edu.agh.kt.MapBook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;
import pl.edu.agh.kt.MapBook.utils.DBHelper;
import pl.edu.agh.kt.MapBook.utils.Position;

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
    List<Position> coordinates;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.explorer);
        mapView = (ImageView) findViewById(R.id.mapView);
        coordinates = new ArrayList<Position>(2);

        DBHelper db = new DBHelper(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.explorer_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new_map:
                loadNewMap();
                return true;
            case R.id.action_save_map:
                saveMap();
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
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();

            mapView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

            mapView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (CAN_DRAW_POINT) {
                        Log.i("Drawing new Point at:", "x=" + String.valueOf(event.getRawX()) + ", y=" + String.valueOf(event.getRawY()));
                        drawPoint(event.getRawX(), event.getRawY());
                    }
                    return true;
                }
            });
        }
    }

    private void enableDrawing() {
        CAN_DRAW_POINT = true;
    }

    private void drawPoint(final float x, final float y) {

        CAN_DRAW_POINT = false;

        AlertDialog.Builder builder = new AlertDialog.Builder(ExplorerActivity.this);
        LayoutInflater inflater = ExplorerActivity.this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_coordinate, null));
        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                EditText latEditText = (EditText) ((AlertDialog) dialog).findViewById(R.id.latitude);
                EditText lonEditText = (EditText) ((AlertDialog) dialog).findViewById(R.id.longitude);
                if (coordinates.size() == 2) coordinates.remove(0);
                coordinates.add(new Position(x, y, latEditText.getText().toString(), lonEditText.getText().toString()));
                buildMap();
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //
            }
        });
        builder.setTitle(R.string.dialog_title);
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void buildMap() {
        Bitmap myBitmap = BitmapFactory.decodeFile(picturePath);
        Paint myPosPaint = new Paint();
        myPosPaint.setColor(Color.rgb(0, 0, 255));
        myPosPaint.setStrokeWidth(5);

        Bitmap tempBitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas tempCanvas = new Canvas(tempBitmap);
        tempCanvas.drawBitmap(myBitmap, 0, 0, null);
        for (Position pos : coordinates) {
            tempCanvas.drawRoundRect(new RectF(pos.getScreenX() - 5, pos.getScreenY() - 5, pos.getScreenX() + 5, pos.getScreenY() + 5), 2, 2, myPosPaint);
        }
        mapView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
    }

    private void saveMap() {
        if(coordinates.size()<2) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(ExplorerActivity.this);
        LayoutInflater inflater = ExplorerActivity.this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_map_title, null));
        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                EditText titleEditText = (EditText) ((AlertDialog) dialog).findViewById(R.id.map_title);
                String mapTitle = titleEditText.getText().toString();
                String scale = calculateMapScale(coordinates);
                saveMapToDB();
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //
            }
        });
        builder.setTitle(R.string.dialog_map_title);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String calculateMapScale(List<Position> coordinates) {
        Position pos1 = coordinates.get(0);
        Position pos2 = coordinates.get(1);

        // instantiate the calculator
        GeodeticCalculator geoCalc = new GeodeticCalculator();

        // select a reference elllipsoid
        Ellipsoid reference = Ellipsoid.WGS84;

        // set first coordinates
        GlobalCoordinates firstCoordinates;
        firstCoordinates = new GlobalCoordinates(Double.parseDouble(pos1.getLatitude()), Double.parseDouble(pos1.getLongitude()));
        // set second coordinates
        GlobalCoordinates secondCoordinates;
        secondCoordinates = new GlobalCoordinates(Double.parseDouble(pos2.getLatitude()), Double.parseDouble(pos2.getLongitude()));

        // calculate the geodetic curve
        GeodeticCurve geoCurve = geoCalc.calculateGeodeticCurve(reference, firstCoordinates, secondCoordinates);
        double ellipseCm = geoCurve.getEllipsoidalDistance();
        Log.w("Ellipsoidal Distance: ", Double.valueOf(ellipseCm).toString());

        Log.w("Screen pos1: ","x: "+pos1.getScreenX()+", y:"+pos1.getScreenY());
        Log.w("Screen pos2: ","x: "+pos2.getScreenX()+", y:"+pos2.getScreenY());
        double imgDistance = Math.sqrt(Math.pow(pos1.getScreenX()-pos2.getScreenX(),2)+Math.pow(pos1.getScreenY()-pos2.getScreenY(),2))/100;
        Log.w("Screen Distance: ", Double.valueOf(imgDistance).toString());

        double scale = ellipseCm/imgDistance;

        return Double.valueOf(scale).toString();
    }

    private void saveMapToDB() {

    }

}