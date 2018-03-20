package com.lesjaw.wamonitoring.ui.fragment;

import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.lesjaw.wamonitoring.NetworkRequest;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.adapter.CheckListAdapterData;
import com.lesjaw.wamonitoring.adapter.ListTagsDailyAdapter;
import com.lesjaw.wamonitoring.model.checklistModelData;
import com.lesjaw.wamonitoring.model.tagsDailyModel;
import com.lesjaw.wamonitoring.ui.EmployeeProfile;
import com.lesjaw.wamonitoring.utils.Config;
import com.lesjaw.wamonitoring.utils.EndlessRecyclerViewScrollListener;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;
import com.lesjaw.wamonitoring.utils.Utils;
import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SlideUpBuilder;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class LogDaily extends android.support.v4.app.Fragment {

    //private RecyclerView.LayoutManager linlayoutManager;
    private SharedPreferences sharedPref;
    private PreferenceHelper mPrefHelper;
    private static final String TAG = "LogDaily";
    private List<tagsDailyModel> tagList = new ArrayList<>();
    private ListTagsDailyAdapter mAdapter;
    private String mLevelUser;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView tagdata;
    private SlideUp slideUp;
    private View dim;
    private List<checklistModelData> tagList1 = new ArrayList<>();
    private CheckListAdapterData mAdapterCklist;
    private ListView lv;
    private EndlessRecyclerViewScrollListener scrollListener;
    private int firstpage = 10;
    private static final String IMAGE_DIRECTORY = "/wamonitoring";
    private String mCompanyID, mDivision, mEmail;
    private StyleableToast styleableToast;
    boolean requestViewByEmail;

    public LogDaily() {
        requestViewByEmail = false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        Bundle bundle = this.getArguments();
        mEmail = bundle.getString("message");
        requestViewByEmail = !mEmail.equals("none");

        //Log.d(TAG, "onCreateView: "+mEmail);
        return inflater.inflate(R.layout.frag_log_daily, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        View sliderView = view.findViewById(R.id.slideViewLeft);
        tagdata = (TextView) view.findViewById(R.id.textView1);
        lv = (ListView) view.findViewById(R.id.listview_checklist);
        FloatingActionButton share = (FloatingActionButton) view.findViewById(R.id.share);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        mPrefHelper = new PreferenceHelper(getContext());

        mCompanyID = sharedPref.getString("company_id", "olmatix1");
        mDivision = sharedPref.getString("division", "olmatix1");

        dim = view.findViewById(R.id.dim);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        Context mContext = getActivity();
        RecyclerView mRecycleView = (RecyclerView) view.findViewById(R.id.rv);
        mRecycleView.setHasFixedSize(true);

        mAdapter = new ListTagsDailyAdapter(tagList, mContext);
        LinearLayoutManager linlayoutManager = new LinearLayoutManager(getContext());
        mRecycleView.setLayoutManager(linlayoutManager);
        mRecycleView.setItemAnimator(new DefaultItemAnimator());
        mRecycleView.setAdapter(mAdapter);

        sliderView.setOnClickListener(v -> slideUp.hide());

        slideUp = new SlideUpBuilder(sliderView)
                .withListeners(new SlideUp.Listener.Events() {
                    @Override
                    public void onSlide(float percent) {
                        dim.setAlpha(1 - (percent / 100));
                    }

                    @Override
                    public void onVisibilityChanged(int visibility) {
                        if (visibility == View.GONE) {
                            dim.setAlpha(0);
                        }
                    }
                })
                .withStartGravity(Gravity.END)
                .withLoggingEnabled(false)
                .withGesturesEnabled(false)
                .withStartState(SlideUp.State.HIDDEN)
                .build();


        getData("0");

        scrollListener = new EndlessRecyclerViewScrollListener(linlayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                Log.d(TAG, "last position and firtpage is " + firstpage);
                /*if (countLoad==1){
                    firstpage = 10;
                } else */
                loadNextDataFromApi(String.valueOf(firstpage));
                firstpage = firstpage + 10;
            }
        };
        mRecycleView.addOnScrollListener(scrollListener);

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            tagList.clear();
            mAdapter.notifyDataSetChanged();
            scrollListener.resetState();
            firstpage = 10;
            setRefresh();
        });

        mRecycleView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
                mRecycleView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) throws NoSuchFieldException, IllegalAccessException {
                String EmployeName = tagList.get(position).getEmployee_name();
                CircleImageView imgProfile = (CircleImageView) view.findViewById(R.id.img_profile);

                imgProfile.setOnClickListener(v -> {
                    Intent i = new Intent(getActivity(), EmployeeProfile.class);
                    i.putExtra("email", tagList.get(position).getEmail());
                    String transitionName = getString(R.string.imgProfile);

                    ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity(), imgProfile, transitionName);
                    startActivity(i, transitionActivityOptions.toBundle());
                });

                ImageView cklist = (ImageView) view.findViewById(R.id.cklist);
                cklist.setOnClickListener(v -> {
                    tagdata.setText(EmployeName + " | " + tagList.get(position).getTag_name() + "\nDivision : " + tagList.get(position).getDivision_name());

                    GetChecklistItem(tagList.get(position).getTrid());
                });

                styleableToast = new StyleableToast
                        .Builder(getContext())
                        .icon(R.mipmap.barcode_img)
                        .text("This tag was tagged at "+ Utils.address(Double.parseDouble(tagList.get(position).getLatitude()),
                                Double.parseDouble(tagList.get(position).getLongitude()),
                                getContext()))
                        .textColor(Color.WHITE)
                        .backgroundColor(Color.BLUE)
                        .build();
                styleableToast.show();

            }

            @Override
            public void onLongClick(View view, int position) {

            }

        }));


        share.setOnClickListener(v -> {
            //saveImage(bitmap);
            //Toast.makeText(getContext(),"We are still working on exporting report",Toast.LENGTH_SHORT).show();
            styleableToast = new StyleableToast
                    .Builder(getContext())
                    .icon(R.drawable.ic_action_info)
                    .text("We are still working on exporting report")
                    .textColor(Color.WHITE)
                    .backgroundColor(Color.BLUE)
                    .build();
            styleableToast.show();

            // Destination Folder and File name
            String FILE = Environment.getExternalStorageDirectory().toString()
                    + IMAGE_DIRECTORY + "/PDF/" + "Name.pdf";

            // Create New Blank Document
            Document document = new Document(PageSize.A4);

            // Create Directory in External Storage
            String root = Environment.getExternalStorageDirectory().toString() + IMAGE_DIRECTORY;
            File myDir = new File(root + "/PDF");
            myDir.mkdirs();

            // Create Pdf Writer for Writting into New Created Document
            try {
                PdfWriter.getInstance(document, new FileOutputStream(FILE));
                // Open Document for Writting into document
                document.open();

                // User Define Method
                addMetaData(document);
                addTitlePage(document);
                // Close Document after writting all content
                document.close();

                /*File wallpaperDirectory = new File(
                        Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY + "/report");
                // have the object build the directory structure, if needed.
                if (!wallpaperDirectory.exists()) {
                    wallpaperDirectory.mkdirs();
                }*/


                //File f = FILE;

            } catch (DocumentException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        });
    }

    public void addMetaData(Document document) {
        document.addTitle("Log Daily");
        document.addSubject("WAMonitoring");
        document.addKeywords("Monitoring");
        document.addAuthor(mCompanyID);
        document.addCreator(mDivision);
    }

    public void addTitlePage(Document document) throws DocumentException {
        // Font Style for Document
        Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
        Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 22, Font.BOLD
                | Font.UNDERLINE, BaseColor.GRAY);
        Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
        Font normal = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);

        // Start New Paragraph
        Paragraph prHead = new Paragraph();
        // Set Font in this Paragraph
        prHead.setFont(titleFont);
        // Add item into Paragraph
        prHead.add("WAMonitoring – " + mCompanyID + "\n");

        // Create Table into Document with 1 Row
        PdfPTable myTable = new PdfPTable(1);
        // 100.0f mean width of table is same as Document size
        myTable.setWidthPercentage(100.0f);

        // Create New Cell into Table
        PdfPCell myCell = new PdfPCell(new Paragraph(""));
        myCell.setBorder(Rectangle.BOTTOM);

        // Add Cell into Table
        myTable.addCell(myCell);

        prHead.setFont(catFont);
        prHead.add("\nDate: 11-10-12017\n");
        prHead.setAlignment(Element.ALIGN_CENTER);

        // Add all above details into Document
        document.add(prHead);
        document.add(myTable);
        document.add(myTable);

        // Now Start another New Paragraph
        Paragraph prPersinalInfo = new Paragraph();
        prPersinalInfo.setFont(smallBold);
        prPersinalInfo.add("Address 1\n");
        prPersinalInfo.add("Address 2\n");
        prPersinalInfo.add("City: SanFran.  State: CA\n");
        prPersinalInfo.add("Country: USA Zip Code: 000001\n");
        prPersinalInfo.add("Mobile: 9999999999 Fax: 1111111 Email: john_pit@gmail.com \n");

        prPersinalInfo.setAlignment(Element.ALIGN_CENTER);

        document.add(prPersinalInfo);
        document.add(myTable);
        document.add(myTable);

        Paragraph prProfile = new Paragraph();
        prProfile.setFont(smallBold);
        prProfile.add("\n \n Profile : \n ");
        prProfile.setFont(normal);
        prProfile.add("\nI am Mr. XYZ. I am Android Application Developer at TAG.");

        prProfile.setFont(smallBold);
        document.add(prProfile);

        // Create new Page in PDF
        document.newPage();
    }

    public void loadNextDataFromApi(String page) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        mPrefHelper = new PreferenceHelper(getContext());

        String mCompanyID = sharedPref.getString("company_id", "olmatix1");
        String mDivision;
        if(mLevelUser.equals("4")){
            mDivision = mPrefHelper.getGroup();
        }else{
            mDivision = sharedPref.getString("division", "olmatix1");
        }

        mLevelUser = mPrefHelper.getLevelUser();

        String url;
        if (!requestViewByEmail) {
            if (mLevelUser.equals("0")) {
                url = Config.DOMAIN + "wamonitoring/get_tags_record_data.php";
            } else if(mLevelUser.equals("4")) {
                url = Config.DOMAIN + "wamonitoring/get_tags_record_data_byGroup.php";
            } else {
                url = Config.DOMAIN + "wamonitoring/get_tags_record_data_byDivision.php";
            }
        } else {
            url = Config.DOMAIN + "wamonitoring/get_tags_record_data_byEmail.php";
        }

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            JSONObject jsonResponse = null;
            try {
                jsonResponse = new JSONObject(response);
                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                if (result_code.equals("1")) {
                    JSONArray cast = jsonResponse.getJSONArray("tags");

                    //tagList.clear();

                    for (int i = 0; i < cast.length(); i++) {

                        JSONObject tags_name = cast.getJSONObject(i);
                        String employee_name = tags_name.getString("employee_name");
                        String tag_name = tags_name.getString("tag_name");
                        String tid = tags_name.getString("tid");
                        String trid = tags_name.getString("trid");
                        String range_loc = tags_name.getString("range_loc");
                        String division_name = tags_name.getString("division_name");
                        //String div_id = tags_name.getString("div_id");
                        String tgl = tags_name.getString("tgl");
                        String jam = tags_name.getString("jam");
                        String checklist_done = tags_name.getString("checklist_done");
                        String email = tags_name.getString("email");
                        String before_foto = tags_name.getString("before_foto");
                        String after_foto = tags_name.getString("after_foto");
                        String latitude = tags_name.getString("latitude");
                        String longitude = tags_name.getString("longitude");

                        tagsDailyModel tags = new tagsDailyModel(employee_name, tag_name, range_loc,
                                division_name, "", tgl, jam, tid, checklist_done, trid, email, after_foto,
                                before_foto,latitude, longitude);
                        tagList.add(tags);
                        //Log.d(TAG, "setData: " + employee_name);

                    }
                    mAdapter.notifyDataSetChanged();


                }
            } catch (JSONException e) {
                e.printStackTrace();
                mSwipeRefreshLayout.setRefreshing(false);
            }

        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("company_id", mCompanyID);
                MyData.put("division", mDivision);
                MyData.put("offsett", page);
                MyData.put("email", mEmail);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);
    }

    private void setRefresh() {
        getData("0");
    }

    private void getData(String page) {
        mSwipeRefreshLayout.setRefreshing(true);

        mLevelUser = mPrefHelper.getLevelUser();

        String url;
        if (!requestViewByEmail) {
            if (mLevelUser.equals("0")) {
                url = Config.DOMAIN + "wamonitoring/get_tags_record_data.php";
            } else {
                url = Config.DOMAIN + "wamonitoring/get_tags_record_data_byDivision.php";
            }
        } else {
            url = Config.DOMAIN + "wamonitoring/get_tags_record_data_byEmail.php";
        }

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            JSONObject jsonResponse = null;
            try {
                jsonResponse = new JSONObject(response);

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                if (result_code.equals("1")) {
                    JSONArray cast = jsonResponse.getJSONArray("tags");

                    tagList.clear();

                    for (int i = 0; i < cast.length(); i++) {

                        JSONObject tags_name = cast.getJSONObject(i);
                        String employee_name = tags_name.getString("employee_name");
                        String tag_name = tags_name.getString("tag_name");
                        String tid = tags_name.getString("tid");
                        String trid = tags_name.getString("trid");
                        String range_loc = tags_name.getString("range_loc");
                        String division_name = tags_name.getString("division_name");
                        String tgl = tags_name.getString("tgl");
                        String jam = tags_name.getString("jam");
                        String checklist_done = tags_name.getString("checklist_done");
                        String email = tags_name.getString("email");
                        String before_foto = tags_name.getString("before_foto");
                        String after_foto = tags_name.getString("after_foto");
                        String latitude = tags_name.getString("latitude");
                        String longitude = tags_name.getString("longitude");

                        tagsDailyModel tags = new tagsDailyModel(employee_name, tag_name, range_loc,
                                division_name, "", tgl, jam, tid, checklist_done, trid, email, after_foto,
                                before_foto,latitude, longitude);
                        tagList.add(tags);
                        //Log.d(TAG, "setData LogDaily: " + employee_name);

                    }
                    mAdapter.notifyDataSetChanged();

                    mSwipeRefreshLayout.setRefreshing(false);


                } else {
                    mSwipeRefreshLayout.setRefreshing(false);

                }

            } catch (JSONException e) {
                e.printStackTrace();
                mSwipeRefreshLayout.setRefreshing(false);
            }

        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("company_id", mCompanyID);
                MyData.put("division", mDivision);
                MyData.put("offsett", page);
                MyData.put("email", mEmail);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public interface ClickListener {
        void onClick(View view, int position) throws NoSuchFieldException, IllegalAccessException;

        void onLongClick(View view, int position);
    }

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final ClickListener clicklistener) {

            this.clicklistener = clicklistener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recycleView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clicklistener != null) {
                        clicklistener.onLongClick(child, recycleView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clicklistener != null && gestureDetector.onTouchEvent(e)) {
                try {
                    clicklistener.onClick(child, rv.getChildAdapterPosition(child));
                } catch (NoSuchFieldException | IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }

            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    private void GetChecklistItem(String tagRID) {

        String url = Config.DOMAIN + "wamonitoring/get_checklist_data.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            JSONObject jsonResponse = null;
            try {
                jsonResponse = new JSONObject(response);

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                if (result_code.equals("1")) {

                    JSONArray cast = jsonResponse.getJSONArray("tags");
                    tagList1.clear();
                    for (int i = 0; i < cast.length(); i++) {

                        JSONObject tags_name = cast.getJSONObject(i);
                        String trid = tags_name.getString("trid");
                        String checklistName = tags_name.getString("checklist");
                        String checklistStatus = tags_name.getString("checklist_status");
                        String checklistNote = tags_name.getString("checklist_note");
                        boolean cklstatus;
                        cklstatus = checklistStatus.equals("1");

                        checklistModelData tags = new checklistModelData(trid, checklistName, checklistStatus, checklistNote, cklstatus);
                        tagList1.add(tags);
                        Log.d("DEBUG", "setData: " + checklistName + " " + cklstatus);
                    }


                    mAdapterCklist = new CheckListAdapterData(getContext(), tagList1);
                    lv.setAdapter(mAdapterCklist);
                    slideUp.hide();
                    slideUp.show();


                } else {
                    slideUp.hide();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("trid", tagRID);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);
    }


    private void shareImage(File file) {
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("pdf/*");

        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, "");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        try {
            startActivity(Intent.createChooser(intent, "Share Report"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "No App Available", Toast.LENGTH_SHORT).show();
        }
    }

}
