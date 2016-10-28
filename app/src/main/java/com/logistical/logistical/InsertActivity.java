package com.logistical.logistical;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.logistical.model.Order;
import com.logistical.model.Staff;
import com.logistical.tools.ErrorHandler;
import com.logistical.tools.Porting;
import com.logistical.tools.PrintWork;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
public class InsertActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    HashMap<String, EditText> mse = new HashMap<String, EditText>();
    HashMap<String, Spinner> mss = new HashMap<String, Spinner>();
    private int staffSelected = 1;
    private ArrayList<String> StaffString = new ArrayList<>();
    private ArrayAdapter<String> StaffAdapter;
    private ListViewAdapter listviewadapter;
    private final int REQUEST_ENABLE_BT = 1;
    private static final String edit[] = {"Fstation2", "Tstation2", "danhao",
            "Fname", "Ftel", "Tname", "Ttel", "number", "uniprice", "daishou", "fankuan", "baojia", "jiehuo", "songyun",
            "totnumber", "tottranpay", "totpay"};
    private final String[] FEE = {"代收款", "返款费", "保价费", "接货费", "送货费", "总件数", "总运费", "总价"};
    private final String[] ATTR = {"发站", "到站", "客户单号", "发货人", "发货人电话", "收货人", "收货人电话", "付款方式",
            "返款方", "返款方式1", "返款方式2"};
    public static final String list[] = {
            "staffnum", "payway", "category1", "category2", "Fstation", "Tstation", "fankuanfang","Ffankuan", "Tfankuan"
    };
    BroadcastReceiver mReceiver;
    private String ID;
    private int totindex = 1;
    private Button addStaff, saveStaff, confirm;
    private ListView listview;
    private Staff staff[] = new Staff[100];
    private View insert_layout, query_layout;
    private ArrayList<Order> OrderList=new ArrayList<Order>();
    private ArrayList<Order> neworder = new ArrayList<Order>();
    private String MAC;
    private EditText py1,py2;
    private Order order;
    Porting pt = new Porting(new ErrorHandler() {
        @Override
        public void onError(String msg) {
            Log.e("porting", msg);
        }
    });

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);
        ID = getIntent().getStringExtra("ID");
        MAC = getIntent().getStringExtra("MAC");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert drawer != null;
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Log.d("test","init");
        try {
            init();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        } catch (NoSuchFieldException e1) {
            e1.printStackTrace();
        }

        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                try {
                    save();
                    staffSelected = Integer.parseInt(mss.get("staffnum").getSelectedItem().toString());
                } catch (NullValueException e) {
                    e.printStackTrace();
                    Toast.makeText(InsertActivity.this,"有表单尚未完成",Toast.LENGTH_LONG).show();
                }
                int id = item.getItemId();
                if (id == R.id.query) {
                    insert_layout.setVisibility(View.GONE);
                    query_layout.setVisibility(View.VISIBLE);
                    listview = (ListView) findViewById(R.id.list_view);
                    for(Order od:OrderList){
                        Log.d("list",od.toJson());
                    }
                    if (OrderList.size()==0){
                        Toast.makeText(InsertActivity.this,"当前无任何记录",Toast.LENGTH_LONG).show();
                        query_layout.setVisibility(View.GONE);
                        insert_layout.setVisibility(View.VISIBLE);
                    }
                    else {
                        listviewadapter = new ListViewAdapter(InsertActivity.this, R.layout.item, OrderList);
                        listview.setAdapter(listviewadapter);
                        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                ;
                                Order order = OrderList.get(position);
                                Log.e("aaa", "bbb" + order.getAttribute("客户单号"));
                                Intent intent = new Intent(InsertActivity.this, detailActivity.class);
                                String Sorder = order.toJson();
                                intent.putExtra("ID", ID);
                                intent.putExtra("MAC", MAC);
                                intent.putExtra("order", Sorder);
                                startActivity(intent);
                            }
                        });
                    }

                } else if (id == R.id.insert) {
                    staffSelected = 1;
                    insert_layout.setVisibility(View.VISIBLE);
                    query_layout.setVisibility(View.GONE);
                    staff = new Staff[100];
                    totindex = 1;
                    for (int i=0;i<edit.length;i++) {
                        if(i<=13&&i>=9)   mse.get(edit[i]).setText("0");
                        else mse.get(edit[i]).setText("");
                    }
                    staff[1] = new Staff();
                    StaffString = new ArrayList<String>();
                    StaffString.add("1");
                    StaffAdapter = new ArrayAdapter<String>(InsertActivity.this, android.R.layout.simple_spinner_item, StaffString);
                    StaffAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
                    mss.get("staffnum").setAdapter(StaffAdapter);
                    for (String anList : list) {
                        Log.e("anlist", anList);
                        mss.get(anList).setSelection(0);
                    }

                } else if (id == R.id.export) {
                    exportFile();
                }
                else if (id == R.id.print) {
                    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (mBluetoothAdapter == null) {
                        Toast.makeText(InsertActivity.this, "设备不支持蓝牙", Toast.LENGTH_LONG).show();

                    }
                    if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
                        Toast.makeText(InsertActivity.this, "未打开蓝牙", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intent, REQUEST_ENABLE_BT);
                        mBluetoothAdapter.startDiscovery();
                        intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        //设置可被发现的时间，300s
                        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                        startActivity(intent);
                    }
                    if (mBluetoothAdapter != null) {
                        mBluetoothAdapter.startDiscovery();
                    }
                    mReceiver = new BroadcastReceiver() {
                        public void onReceive(Context context, Intent intent) {
                            String action = intent.getAction();
                            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                            }
                        }
                    };
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
                    Set<BluetoothDevice> pairedDevices = null;
                    if (mBluetoothAdapter != null) {
                        pairedDevices = mBluetoothAdapter.getBondedDevices();
                    }
                    BluetoothDevice useDevice = null;
                    if ((pairedDevices != null ? pairedDevices.size() : 0) > 0) {
                        for (BluetoothDevice device : pairedDevices) {
                            if (device.getAddress().equals(MAC)) {
                                useDevice = device;
                            }
                        }
                    }
                    if (useDevice != null) {
                        ConnectThread cnt = new ConnectThread(useDevice, null);
                        cnt.start();
                    }
                    else {
                        Toast.makeText(InsertActivity.this,"请先完成配对",Toast.LENGTH_LONG).show();
                    }
                }

                item.setCheckable(true);
                drawer.closeDrawers();
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                assert drawer != null;
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        View header = navigationView.getHeaderView(0);
        TextView TextForId = (TextView) header.findViewById(R.id.TextForID);
        TextForId.setText("" + ID);
        mss.get("staffnum").setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                
                int tmp = Integer.parseInt(parent.getSelectedItem().toString());
                Log.d("staff",""+tmp);
                Log.d("staff",staff[tmp].toString());
                if (staff[tmp].getNumber()!=0) {
                    mse.get("number").setText(""+staff[tmp].getNumber());
                    mse.get("uniprice").setText(""+staff[tmp].getPrice());
                    Spinner cate1 = mss.get("category1");
                    for (int i = 0; i < cate1.getAdapter().getCount(); i++) {
                        if (cate1.getAdapter().getItem(i).toString().equals(staff[tmp].getType()))
                            cate1.setSelection(i);
                    }
                    Spinner cate2 = mss.get("category2");
                    for (int i = 0; i < cate1.getAdapter().getCount(); i++) {
                        if (cate1.getAdapter().getItem(i).toString().equals(staff[tmp].getSubType()))
                            cate2.setSelection(i);
                    }
                }
                if(tmp<totindex) addStaff.setVisibility(View.GONE);
                else addStaff.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        addStaff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    save();
                } catch (NullValueException e1) {
                    Toast.makeText(InsertActivity.this, "存在未完成的表单", Toast.LENGTH_LONG).show();
                    return;
                }
                StaffString.add("" + (++totindex));
                staff[totindex]=new Staff();
                // StaffAdapter.add("" + (++totindex));
                mss.get("staffnum").setSelection(totindex - 1);
                mss.get("category1").setSelection(0);
                mss.get("category2").setSelection(0);
                mse.get("uniprice").setText("");
                mse.get("number").setText("");
                Toast.makeText(InsertActivity.this,"保存并增加成功",Toast.LENGTH_LONG).show();
            }
        });
        saveStaff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    save();
                    Toast.makeText(InsertActivity.this,"保存成功",Toast.LENGTH_LONG).show();
                } catch (NullValueException e) {
                    Toast.makeText(InsertActivity.this, "存在未完成的表单", Toast.LENGTH_LONG).show();
                }

            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Order order=null;
                try {
                    save();
                  order= makeOrder();
                    Toast.makeText(InsertActivity.this,"保存成功当前单号:"+Order.getBarcode(""+py1.getText()+py2.getText(),new Date(),Integer.parseInt(ID)),Toast.LENGTH_LONG).show();
                } catch (NullValueException e1) {
                    Toast.makeText(InsertActivity.this, "存在未完成的表单", Toast.LENGTH_LONG).show();
                } catch (NotNumberException e2) {
                    Toast.makeText(InsertActivity.this, "请正确填写数字", Toast.LENGTH_LONG).show();
                }

            }
        });
        Log.d("test","Fstation"+(mss.get("Fstation")));
        ((Spinner)mss.get("Fstation")).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==2){
                    findViewById(R.id.main_content).findViewById(R.id.insert_layout).findViewById(R.id.Fstation2_T).setVisibility(View.VISIBLE);
                    py1.setText("");
                    mse.get("Fstation2").setText("");
                }
                else {
                    if(position==0){
                        py1.setText("cd");
                        mse.get("Fstation2").setText("成都");

                    }
                    else {
                        py1.setText("pzh");
                        mse.get("Fstation2").setText("攀枝花");
                    }
                    findViewById(R.id.main_content).findViewById(R.id.insert_layout).findViewById(R.id.Fstation2_T).setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });
        mss.get("Tstation").setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("tstaion","in");
                if(position==2){
                    findViewById(R.id.main_content).findViewById(R.id.insert_layout).findViewById(R.id.Tstation2_T).setVisibility(View.VISIBLE);
                    py2.setText("");
                    mse.get("Tstation2").setText("");
                }
                else {
                    if(position==0){
                        py2.setText("cd");
                        mse.get("Tstation2").setText("成都");
                    }
                    else {
                        py2.setText("pzh");
                        mse.get("Tstation2").setText("攀枝花");
                    }
                    findViewById(R.id.main_content).findViewById(R.id.insert_layout).findViewById(R.id.Tstation2_T).setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });
    }

    private void init() throws NoSuchFieldException, IllegalAccessException {
        Log.d("test","null:"+(findViewById(R.id.main_content).findViewById(R.id.insert_layout)==null));
        addStaff = (Button) findViewById(R.id.main_content).findViewById(R.id.insert_layout).findViewById(R.id.Addstaff);
        saveStaff = (Button) findViewById(R.id.main_content).findViewById(R.id.insert_layout).findViewById(R.id.Savestaff);
        confirm = (Button) findViewById(R.id.main_content).findViewById(R.id.insert_layout).findViewById(R.id.confirm);
        query_layout = findViewById(R.id.query_layout);
        insert_layout = findViewById(R.id.insert_layout);
        query_layout.setVisibility(View.GONE);
        insert_layout.setVisibility(View.VISIBLE);
        staff[1] = new Staff();
        for (String anEdit : edit) {
            Field r = R.id.class.getField(anEdit);
            mse.put(anEdit, (EditText) findViewById(R.id.main_content).findViewById(R.id.insert_layout).findViewById(r.getInt(null)));
            Log.d("test",anEdit);
        }
        for (String aList : list) {
            Field r = R.id.class.getField(aList);
            mss.put(aList, (Spinner) findViewById(R.id.main_content).findViewById(R.id.insert_layout).findViewById(r.getInt(null)));
            Log.d("test",aList);
        }
        py1 = (EditText) findViewById(R.id.main_content).findViewById(R.id.insert_layout).findViewById(R.id.py1);
        py2 = (EditText) findViewById(R.id.main_content).findViewById(R.id.insert_layout).findViewById(R.id.py2);
        StaffString.add("1");
        StaffAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, StaffString);
        StaffAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        mss.get("staffnum").setAdapter(StaffAdapter);
        Log.d("test","init+Fstation"+(mss.get("Fstation")==null));
        for(int i=9;i<=13;i++) {
            mse.get(edit[i]).setText("0");
        }
        getFile();
        Log.d("filereader",OrderList.toString());
    }

    private void save() throws NullValueException {
        int index = staffSelected;
        try {
            Staff temstaff = new Staff(mss.get("category1").getSelectedItem().toString(), mss.get("category2").getSelectedItem().toString(),
                    Integer.parseInt(mse.get("number").getText().toString()), Integer.parseInt(mse.get("uniprice").getText().toString()));
            staff[index] = temstaff;
        } catch (Exception e) {
            Toast.makeText(InsertActivity.this, "不能有空或者非数字", Toast.LENGTH_SHORT).show();
            throw new NullValueException();
        }
        // TODO 判断空
        Log.e("save", staff[index].toString());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
    }

    /*
    TODO: 把保存的过程都inline了, 可以从日志里查看是否写成功
     */
    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(mReceiver);
            Log.d("addfile","out");
        }catch(Exception e){
            Log.d("stop","not register");
        }
        finally {
            try {
                FileOutputStream fos = openFileOutput("message.txt", MODE_PRIVATE);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
                Log.d("save",""+OrderList.size());

                pt.saveOrder(OrderList, writer);
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        return true;
    }

    private Order makeOrder() throws NullValueException, NotNumberException {
        IdentityHashMap<String, String> attributes = new IdentityHashMap<String, String>();
        IdentityHashMap<String, Integer> fee = new IdentityHashMap<>();
        for (int i = 9; i <=13; i++) {
            if ((mse.get(edit[i]).getText().toString().equals(""))) throw new NullValueException();
            try {
                fee.put(FEE[i - 9], Integer.parseInt(mse.get(edit[i]).getText().toString()));
            } catch (Exception e) {
                Log.d("order","1:"+i);
                throw new NotNumberException();
            }
        }
        for (int i = 0; i <= 6; i++) {
            attributes.put(ATTR[i], mse.get(edit[i]).getText().toString());
        }
        attributes.put("返款方", mss.get("fankuanfang").getSelectedItem().toString());
        attributes.put("付款方式", mss.get("payway").getSelectedItem().toString());
        attributes.put("返款方式1", mss.get("Ffankuan").getSelectedItem().toString());
        attributes.put("返款方式2", mss.get("Tfankuan").getSelectedItem().toString());
        //TODO 验证合法性
        List<Staff> tmp = new ArrayList<Staff>();
        for(int i=1;i<=totindex;i++) {
            tmp.add(staff[i]);
        }
        order= new Order(attributes, fee, tmp, Order.getBarcode(""+py1.getText()+py2.getText(),new Date(),Integer.parseInt(ID)));
        Log.d("order",order.toJson());
        mse.get("totnumber").setText(""+order.getTotalNumber());
        mse.get("totpay").setText(""+order.getTotalFee());
        mse.get("tottranpay").setText(""+order.getFee("总运费"));
        neworder.add(order);
        OrderList.add(order);
        Log.e("Orderlist",""+OrderList.size());
        return order;
    }
    private void exportFile(){
            FileWriter fileWriter = null;
            try {
                String SDPATH =  Environment.getExternalStorageDirectory().toString();
                String fileName = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+ "日报表.txt";
                Log.d("export",SDPATH+File.separator+ fileName);
                File file = new File(SDPATH + File.separator + fileName);
                if (!file.exists()) {
                    file.createNewFile();
                }
                fileWriter = new FileWriter(SDPATH+File.separator+fileName);
                Log.d("export","aaa");
                pt.expHeader(fileWriter);
              //  pt.exp(OrderList,fileWriter);

            } catch (IOException e) {

                e.printStackTrace();
            }
            pt.exp(OrderList, fileWriter);
            try {
                assert fileWriter != null;
                fileWriter.close();
            } catch (IOException ex) {
                Log.e("export", "export Error2");
            }

    }
    private void getFile(){
        FileInputStream fis = null;
        try {
            File file = new File(getFilesDir()+"/message.txt");
            Log.d("reader",getFilesDir()+"/message.txt");
            if(!file.exists()) {
                FileOutputStream out = null;
                BufferedWriter writer = null;
                Log.d("reader","not");
                out = openFileOutput("message.txt",Context.MODE_PRIVATE);
                writer = new BufferedWriter(new OutputStreamWriter(out));
                writer.write("");
                writer.close();
            }
            fis = openFileInput("message.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        //Log.d("reader","asda"+reader.readLine());
        OrderList.addAll(pt.fetchOrder(reader));
        Log.d("reader",list.toString());
        try {
            reader.close();
        } catch (IOException e) {
            Log.d("reader","2");
            e.printStackTrace();
        }
    }
    private void savef (List<Order> input){
        FileOutputStream out=null;
        BufferedWriter writer = null;
        StringWriter sw = new StringWriter();
        pt.saveOrder(input, sw);
        String json = sw.toString();
        writer = new BufferedWriter(new OutputStreamWriter(out));
        Log.d("json",json);
        try {
             writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    class ConnectThread extends Thread {
        public final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private byte[] mbyte;

        public ConnectThread(BluetoothDevice device, byte[] mbyte) {
            this.mbyte = mbyte;
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // 通过 Bluetooth
                // Device 获得 BluetoothSocket 对象
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
        }

        @Override
        public void run() {
            try {
                mmSocket.connect();
                Log.e("mmconnect", "" + mmSocket.isConnected());
                if (mmSocket.isConnected()) {
                    try {
                        OutputStream outputStream = mmSocket.getOutputStream();
                        Log.e("mmconnect", "" + (outputStream == null));
                        assert outputStream != null;
                        PrintWork.builder().printOrder(order).build(outputStream).run();
                        int tmp = 1;
                        for(int i=1;i<=totindex;i++){
                            Log.d("printwork",staff[i].toString());
                            int id = staff[i].getNumber();
                            for(int j=1;j<=id;j++)
                                PrintWork.builder().printStaff(order,staff[i],tmp++).build(outputStream).run();
                        }
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (mmSocket.isConnected())
                        mmSocket.close();
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                }
            }
        }
    }
}

class NullValueException extends Exception {

}

class NotNumberException extends Exception {

}