package com.example.pooraidwarning;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class mapActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleMap.OnInfoWindowClickListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap googleMap;
    static RequestQueue requestQueue;

    String man;
    int toilets;
    int openHours;
    Boolean childToilet;
    Boolean disabledToilet;
    int urinals;
    int rating;

    boolean check=false;
    SQLiteHelper sqLiteHelper = new SQLiteHelper(this);

    String f_data="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button buttonFinish = findViewById(R.id.buttonFinish);
        Button buttonSearch = findViewById(R.id.buttonSearch);
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();//다시 conditionActivity로 되돌아감
            }
        });
        buttonFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(check) {
                    check=false;
                    Intent intent = new Intent(getApplicationContext(), finishActivity.class);
                    intent.putExtra("title", f_data);//내가 마커 클릭하고 infowindwo 눌러서 갈 곳으로 선택한 화장실의 제목
                    startActivity(intent);
                }
                else{
                    showToast("목적지 마커를 누른 뒤 팝업창에 들어가서 장소 결정을 하세요!");
                }
            }
        });
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }


        Intent receivedIntent = getIntent();
        //검색 조건 데이터들을 가져옴
        man = receivedIntent.getStringExtra("man");
        toilets = Integer.parseInt(receivedIntent.getStringExtra("toilets"));//대변기 수
        openHours = Integer.parseInt(receivedIntent.getStringExtra("openHours"));//개방시간->너무 다양한 데이터가 있어서 parse가 힘들어서 그냥 쓰지 않기로 함.대신 api로 받은 시간 표시
        childToilet = receivedIntent.getBooleanExtra("childToilet", false);//어린이 화장실 유/무
        disabledToilet = receivedIntent.getBooleanExtra("disabledToilet", false); // 장애인 화장실 유/무
        rating=receivedIntent.getIntExtra("stars",0);

        if (Objects.equals(man, "true")) {//남자일 때는 소변기 데이터 추가로 가져옴
            urinals = Integer.parseInt(receivedIntent.getStringExtra("urinals"));
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        callAPI();

        LatLng myLocation = new LatLng(37.6243117, 126.8290417);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 17));if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            googleMap.setMyLocationEnabled(true);
        }
        googleMap.setOnInfoWindowClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                googleMap.setMyLocationEnabled(true);
            } else {
                showToast("위치 권한이 필요합니다.");
                finish();
            }
        }
    }

    public void callAPI(){
        String url;
        for(int i=1;i<10;i++){
            url = "https://openapi.gg.go.kr/Publtolt?KEY=7b49782e7b764c29a2d5814e8a158c8b&pindex="+i+"&psize=1000&type=json";
            StringRequest request = new StringRequest(
                    Request.Method.GET,
                    url,
                    response -> {processResponse(response);}, // 응답 시의 동작
                    error -> {}
            ) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<String,String>();

                    return params;
                }
            };
            request.setShouldCache(false);
            requestQueue.add(request);
        }
    }
    public void processResponse(String response) {
        Gson gson = new Gson();
        Root toiletlist = gson.fromJson(response, Root.class);
        for(int i=0;i<toiletlist.Publtolt.get(1).row.size();i++){
            //null이면 무시
            if(toiletlist.Publtolt.get(1).row.get(i).REFINE_WGS84_LAT ==null || toiletlist.Publtolt.get(1).row.get(i).REFINE_WGS84_LAT ==null)
            {
                continue;
            }
            if(rating>(Math.round(sqLiteHelper.getAvgStar(toiletlist.Publtolt.get(1).row.get(i).PBCTLT_PLC_NM)*100)/100.0)){
                continue;
            }
            if (Objects.equals(man, "true") && (toiletlist.Publtolt.get(1).row.get(i).MALE_UIL_CNT>=urinals)//남자
            && (toiletlist.Publtolt.get(1).row.get(i).MALE_WTRCLS_CNT>=toilets)) {
                if(childToilet && disabledToilet){
                    if((toiletlist.Publtolt.get(1).row.get(i).MALE_KID_WTRCLS_CNT>0||toiletlist.Publtolt.get(1).row.get(i).MALE_KID_UIL_CNT>0)
                    &&toiletlist.Publtolt.get(1).row.get(i).MALE_DSPSN_WTRCLS_CNT>0){
                        mark(toiletlist,i);
                    }
                    else{
                        continue;
                    }
                }
                else if (childToilet) {
                    if((toiletlist.Publtolt.get(1).row.get(i).MALE_KID_WTRCLS_CNT>0||toiletlist.Publtolt.get(1).row.get(i).MALE_KID_UIL_CNT>0)){
                        mark(toiletlist,i);
                    }
                    else{
                        continue;
                    }

                }
                else if (disabledToilet) {
                    if(toiletlist.Publtolt.get(1).row.get(i).MALE_DSPSN_WTRCLS_CNT>0){
                        mark(toiletlist,i);
                    }
                    else{
                        continue;
                    }
                }
                else{
                    mark(toiletlist,i);
                }

            }
            else if(toiletlist.Publtolt.get(1).row.get(i).FEMALE_WTRCLS_CNT>=toilets){ //여자
                if(childToilet && disabledToilet){
                    if(toiletlist.Publtolt.get(1).row.get(i).FEMALE_KID_WTRCLS_CNT>0 &&toiletlist.Publtolt.get(1).row.get(i).FEMALE_DSPSN_WTRCLS_CNT>0){
                        mark(toiletlist,i);
                    }
                    else{
                        continue;
                    }
                }
                else if (childToilet) {
                    if(toiletlist.Publtolt.get(1).row.get(i).FEMALE_KID_WTRCLS_CNT>0){
                        mark(toiletlist,i);
                    }
                    else{
                        continue;
                    }

                }
                else if (disabledToilet) {
                    if(toiletlist.Publtolt.get(1).row.get(i).FEMALE_DSPSN_WTRCLS_CNT>0){
                        mark(toiletlist,i);
                    }
                    else{
                        continue;
                    }
                }
                else{
                    mark(toiletlist,i);
                }
            }
            else{
                continue;
            }
        }
    }
    public void mark(Root toiletlist,int i){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(Double.parseDouble(toiletlist.Publtolt.get(1).row.get(i).REFINE_WGS84_LAT),Double.parseDouble(toiletlist.Publtolt.get(1).row.get(i).REFINE_WGS84_LOGT)));
        markerOptions.title(toiletlist.Publtolt.get(1).row.get(i).PBCTLT_PLC_NM);
        markerOptions.snippet("개방시간 : "+toiletlist.Publtolt.get(1).row.get(i).OPEN_TM_INFO);

        Marker marker=googleMap.addMarker(markerOptions);
        List<Object> mixedList = new ArrayList<>();
        mixedList.add(toiletlist);
        mixedList.add(i);
        marker.setTag(mixedList);
    }
    @Override
    public void onInfoWindowClick(Marker marker) {
        Object tagObject = marker.getTag();
        List<?> mixedList = (List<?>) tagObject;
        Root toiletlist = (Root) mixedList.get(0);
        int i = (int) mixedList.get(1);

        String recentReview=sqLiteHelper.getRecentReview(marker.getTitle());
        double avgStar=Math.round(sqLiteHelper.getAvgStar(marker.getTitle())*100)/100.0;

        /*double avgStar= sqLiteHelper.getAvgStar(marker.getTitle());*/
        //Root toiletlist = (Root) marker.getTag();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(marker.getTitle());
        alertDialogBuilder.setMessage(
                "주소 : "+toiletlist.Publtolt.get(1).row.get(i).REFINE_ROADNM_ADDR+"\n" + "남성-대변기 수 : "+toiletlist.Publtolt.get(1).row.get(i).MALE_WTRCLS_CNT+"\n"
                        + "남성-소변기 수 : "+toiletlist.Publtolt.get(1).row.get(i).MALE_UIL_CNT +"\n"+"남성-어린이용 대변기 수 : "+toiletlist.Publtolt.get(1).row.get(i).MALE_KID_WTRCLS_CNT+"\n"
                        + "남성-어린용 소변기 수 : "+toiletlist.Publtolt.get(1).row.get(i).MALE_KID_UIL_CNT +"\n"+"남성-장애인용 대변기 수 : "+toiletlist.Publtolt.get(1).row.get(i).MALE_DSPSN_WTRCLS_CNT+"\n"
                +"여성-대변기 수 : "+toiletlist.Publtolt.get(1).row.get(i).FEMALE_WTRCLS_CNT+"\n"+"여성-장애인용 대변기 수 : "+toiletlist.Publtolt.get(1).row.get(i).FEMALE_DSPSN_WTRCLS_CNT+"\n"
                        +"여성-어린이용 대변기 수 : "+toiletlist.Publtolt.get(1).row.get(i).FEMALE_KID_WTRCLS_CNT+"\n"+"관리자 번호 : "+toiletlist.Publtolt.get(1).row.get(i).MNGINST_TELNO+"\n"
                +"별점 : "+ avgStar+"\n"+"최근 리뷰 : "+recentReview);

        alertDialogBuilder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // 팝업 창 닫기
            }
        });
        alertDialogBuilder.setPositiveButton("장소 결정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //이곳으로 지정됨 내가 갈곳
                f_data=toiletlist.Publtolt.get(1).row.get(i).PBCTLT_PLC_NM;
                dialog.dismiss(); // 팝업 창 닫기
                check=true;
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
