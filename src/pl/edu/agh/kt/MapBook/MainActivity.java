package pl.edu.agh.kt.MapBook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created with IntelliJ IDEA.
 * User: adba
 * Date: 03.12.13
 */
public class MainActivity extends Activity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
  }

  public void startExplorer(View view){
    Intent intent = new Intent(this, ExplorerActivity.class);
    startActivity(intent);
  }

  public void startCatalogue(View view){
    Intent intent = new Intent(this, CatalogueActivity.class);
    startActivity(intent);
  }

}
