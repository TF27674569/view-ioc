# view-ioc
# 编译时注解库

###   maven { url 'https://jitpack.io' }
###   compile 'com.github.TF27674569:ViewById:v1.1'



## 使用方式 需要调用函数进行注入
`ViewUtils.bindxxx()`
## 相关注解
` @ViewById`    注入控件</br>
` @Extra`       fragment activity 传值</br>
` @Event`       点击事件</br>
` @EchoEnable`  重复点击</br>
` @CheckNet`    检测网络</br>


## 使用方式

public class MainActivity extends AppCompatActivity {
    
    //click1
    @ViewById(R.id.click1)
    public Button click1;
    //click2
    @ViewById(R.id.click2)
    public Button click2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewUtils.bindActivity(this);
    }

    @Event({R.id.click1, R.id.click2})
    @EchoEnable(1000)
    public void click1Click(Button click1) {
        Toast.makeText(this, "click1", Toast.LENGTH_SHORT).show();
    }

    @Event(R.id.click2)
    @CheckNet("可以自己传toast类容， 默认是当前无网络")
    public void click2Click(Button click2) {
        Toast.makeText(this, "click2", Toast.LENGTH_SHORT).show();
    }
}
