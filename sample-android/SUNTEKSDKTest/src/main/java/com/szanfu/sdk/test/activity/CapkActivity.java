package com.szanfu.sdk.test.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.api.constant.ServiceID;
import com.szanfu.sdk.api.emv.IEmvBinderService;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.base.ListButtonBaseActivity;
import com.szanfu.sdk.test.entity.AidItem;
import com.szanfu.sdk.test.entity.CapkEntity;
import com.szanfu.sdk.test.util.AppConfig;
import com.szanfu.sdk.test.util.LogUtils;
import com.szanfu.sdk.test.util.WatermarkUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CapkActivity extends ListButtonBaseActivity implements View.OnClickListener {
    private TextView tv_addCAPKresult;
    private Button btn_add_allCAPK;
    private TextView ed_addCAPKresult;
    private Button btn_addCAPK;
    private TextView tv_delALLCAPK;
    private Button btn_delALLCAPK;
    private EditText ed_updateCAPK;


    private Button btnQueryCAPK;
    private IEmvBinderService mEmvBinderService;
    private ArrayAdapter<AidItem> adapter;
    private Button btn_add_allCAPKoffine,btnQueryCAPKOffline;
    private TextView tv_addCAPKresultoffline;
    private String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/sdcard/Android/DeviceMSG.txt";
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capk);
        tv_addCAPKresult = findViewById(R.id.tv_addCAPKresult);
        btn_add_allCAPK = findViewById(R.id.btn_add_allCAPK);
        btn_add_allCAPK.setOnClickListener(this);
        ed_addCAPKresult = findViewById(R.id.ed_addCAPKresult);
        btn_addCAPK = findViewById(R.id.btn_addCAPK);
        btn_addCAPK.setOnClickListener(this);
        tv_delALLCAPK = findViewById(R.id.tv_delALLCAPK);
        btn_delALLCAPK = findViewById(R.id.btn_delALLCAPK);
        btn_delALLCAPK.setOnClickListener(this);
        btnQueryCAPK = findViewById(R.id.btnQueryCAPK);
        btnQueryCAPK.setOnClickListener(this);
        btn_add_allCAPKoffine = findViewById(R.id.btn_add_allCAPKoffine);
        btn_add_allCAPKoffine.setOnClickListener(this);
        tv_addCAPKresultoffline = findViewById(R.id.tv_addCAPKresultoffline);
        btnQueryCAPKOffline=findViewById(R.id.btnQueryCAPKOffline);
        btnQueryCAPKOffline.setOnClickListener(this);

//        if (AppConfig.buildType != 0){
            setWaterMask();
//        }
    }

    void setWaterMask(){
        String libVersion="";
 // ( )
        WatermarkUtils.addWatermark(this,
                libVersion,
                Color.BLUE, 24, 0, false, 3);
    }



    private String readFile() {
        File file = new File(Environment.getExternalStorageDirectory() + "/Android/DeviceMSG.txt");
        if(!file.exists())
            return "";
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        LogUtils.e("stringBuilder.toString() :"+stringBuilder.toString());
        return stringBuilder.toString();
    }

    @Override
    public void onServiceConnected(ISdkServiceManager sdkServiceManager) {
        try {
            mEmvBinderService = IEmvBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_EMV));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
 // Add all CAPKs CAPK
            case R.id.btn_add_allCAPK:
                String strCapk =
                                "9F0605A0000003339F220101DF028180BBE9066D2517511D239C7BFA77884144AE20C7372F515147E8CE6537C54C0A6A4D45F8CA4D290870CDA59F1344EF71D17D3F35D92F3F06778D0D511EC2A7DC4FFEADF4FB1253CE37A7B2B5A3741227BEF72524DA7A2B7B1CB426BEE27BC513B0CB11AB99BC1BC61DF5AC6CC4D831D0848788CD74F6D543AD37C5A2B4C5D5A93BDF0314E881E390675D44C2DD81234DCE29C3F5AB2297A0DF040103DF05083230303931323331DF060101DF070101\n"
                                +"9F0605A0000003339F220102DF028190A3767ABD1B6AA69D7F3FBF28C092DE9ED1E658BA5F0909AF7A1CCD907373B7210FDEB16287BA8E78E1529F443976FD27F991EC67D95E5F4E96B127CAB2396A94D6E45CDA44CA4C4867570D6B07542F8D4BF9FF97975DB9891515E66F525D2B3CBEB6D662BFB6C3F338E93B02142BFC44173A3764C56AADD202075B26DC2F9F7D7AE74BD7D00FD05EE430032663D27A57DF031403BB335A8549A03B87AB089D006F60852E4B8060DF040103DF05083230323131323331DF060101DF070101\n"
                                +"9F0605A0000003339F220103DF0281B0B0627DEE87864F9C18C13B9A1F025448BF13C58380C91F4CEBA9F9BCB214FF8414E9B59D6ABA10F941C7331768F47B2127907D857FA39AAF8CE02045DD01619D689EE731C551159BE7EB2D51A372FF56B556E5CB2FDE36E23073A44CA215D6C26CA68847B388E39520E0026E62294B557D6470440CA0AEFC9438C923AEC9B2098D6D3A1AF5E8B1DE36F4B53040109D89B77CAFAF70C26C601ABDF59EEC0FDC8A99089140CD2E817E335175B03B7AA33DDF031487F0CD7C0E86F38F89A66F8C47071A8B88586F26DF040103DF05083230323431323331DF060101DF070101\n"
                                +"9F0605A0000003339F220104DF0281F8BC853E6B5365E89E7EE9317C94B02D0ABB0DBD91C05A224A2554AA29ED9FCB9D86EB9CCBB322A57811F86188AAC7351C72BD9EF196C5A01ACEF7A4EB0D2AD63D9E6AC2E7836547CB1595C68BCBAFD0F6728760F3A7CA7B97301B7E0220184EFC4F653008D93CE098C0D93B45201096D1ADFF4CF1F9FC02AF759DA27CD6DFD6D789B099F16F378B6100334E63F3D35F3251A5EC78693731F5233519CDB380F5AB8C0F02728E91D469ABD0EAE0D93B1CC66CE127B29C7D77441A49D09FCA5D6D9762FC74C31BB506C8BAE3C79AD6C2578775B95956B5370D1D0519E37906B384736233251E8F09AD79DFBE2C6ABFADAC8E4D8624318C27DAF1DF0314F527081CF371DD7E1FD4FA414A665036E0F5E6E5DF040103DF05083230323531323331DF060101DF070101\n"
                                +"9F0605A0000000039F220101DF028180C696034213D7D8546984579D1D0F0EA519CFF8DEFFC429354CF3A871A6F7183F1228DA5C7470C055387100CB935A712C4E2864DF5D64BA93FE7E63E71F25B1E5F5298575EBE1C63AA617706917911DC2A75AC28B251C7EF40F2365912490B939BCA2124A30A28F54402C34AECA331AB67E1E79B285DD5771B5D9FF79EA630B75DF0314D34A6A776011C7E7CE3AEC5F03AD2F8CFC5503CCDF040103DF05083230303931323331DF060101DF070101\n"
                                +"9F0605A0000000039F220107DF028190A89F25A56FA6DA258C8CA8B40427D927B4A1EB4D7EA326BBB12F97DED70AE5E4480FC9C5E8A972177110A1CC318D06D2F8F5C4844AC5FA79A4DC470BB11ED635699C17081B90F1B984F12E92C1C529276D8AF8EC7F28492097D8CD5BECEA16FE4088F6CFAB4A1B42328A1B996F9278B0B7E3311CA5EF856C2F888474B83612A82E4E00D0CD4069A6783140433D50725FDF0314B4BC56CC4E88324932CBC643D6898F6FE593B172DF040103DF05083230313231323331DF060101DF070101\n"
                                +"9F0605A0000000039F220108DF0281B0D9FD6ED75D51D0E30664BD157023EAA1FFA871E4DA65672B863D255E81E137A51DE4F72BCC9E44ACE12127F87E263D3AF9DD9CF35CA4A7B01E907000BA85D24954C2FCA3074825DDD4C0C8F186CB020F683E02F2DEAD3969133F06F7845166ACEB57CA0FC2603445469811D293BFEFBAFAB57631B3DD91E796BF850A25012F1AE38F05AA5C4D6D03B1DC2E568612785938BBC9B3CD3A910C1DA55A5A9218ACE0F7A21287752682F15832A678D6E1ED0BDF031420D213126955DE205ADC2FD2822BD22DE21CF9A8DF040103DF05083230313431323331DF060101DF070101\n"
                                +"9F0605A0000000039F220109DF0281F89D912248DE0A4E39C1A7DDE3F6D2588992C1A4095AFBD1824D1BA74847F2BC4926D2EFD904B4B54954CD189A54C5D1179654F8F9B0D2AB5F0357EB642FEDA95D3912C6576945FAB897E7062CAA44A4AA06B8FE6E3DBA18AF6AE3738E30429EE9BE03427C9D64F695FA8CAB4BFE376853EA34AD1D76BFCAD15908C077FFE6DC5521ECEF5D278A96E26F57359FFAEDA19434B937F1AD999DC5C41EB11935B44C18100E857F431A4A5A6BB65114F174C2D7B59FDF237D6BB1DD0916E644D709DED56481477C75D95CDD68254615F7740EC07F330AC5D67BCD75BF23D28A140826C026DBDE971A37CD3EF9B8DF644AC385010501EFC6509D7A41DF03141FF80A40173F52D7D27E0F26A146A1C8CCB29046DF040103DF05083230313631323331DF060101DF070101\n"
                                +"9F0605A0000000659F220109DF028180B72A8FEF5B27F2B550398FDCC256F714BAD497FF56094B7408328CB626AA6F0E6A9DF8388EB9887BC930170BCC1213E90FC070D52C8DCD0FF9E10FAD36801FE93FC998A721705091F18BC7C98241CADC15A2B9DA7FB963142C0AB640D5D0135E77EBAE95AF1B4FEFADCF9C012366BDDA0455C1564A68810D7127676D493890BDDF03144410C6D51C2F83ADFD92528FA6E38A32DF048D0ADF040103DF05083230303931323331DF060101DF070101\n"
                                +"9F0605A0000000659F220110DF02819099B63464EE0B4957E4FD23BF923D12B61469B8FFF8814346B2ED6A780F8988EA9CF0433BC1E655F05EFA66D0C98098F25B659D7A25B8478A36E489760D071F54CDF7416948ED733D816349DA2AADDA227EE45936203CBF628CD033AABA5E5A6E4AE37FBACB4611B4113ED427529C636F6C3304F8ABDD6D9AD660516AE87F7F2DDF1D2FA44C164727E56BBC9BA23C0285DF0314C75E5210CBE6E8F0594A0F1911B07418CADB5BABDF040103DF05083230323231323331DF060101DF070101\n"
                                +"9F0605A0000000659F220112DF0281B0ADF05CD4C5B490B087C3467B0F3043750438848461288BFEFD6198DD576DC3AD7A7CFA07DBA128C247A8EAB30DC3A30B02FCD7F1C8167965463626FEFF8AB1AA61A4B9AEF09EE12B009842A1ABA01ADB4A2B170668781EC92B60F605FD12B2B2A6F1FE734BE510F60DC5D189E401451B62B4E06851EC20EBFF4522AACC2E9CDC89BC5D8CDE5D633CFD77220FF6BBD4A9B441473CC3C6FEFC8D13E57C3DE97E1269FA19F655215B23563ED1D1860D8681DF0314874B379B7F607DC1CAF87A19E400B6A9E25163E8DF040103DF05083230323431323331DF060101DF070101\n"
                                +"9F0605A0000000659F220114DF0281F8AEED55B9EE00E1ECEB045F61D2DA9A66AB637B43FB5CDBDB22A2FBB25BE061E937E38244EE5132F530144A3F268907D8FD648863F5A96FED7E42089E93457ADC0E1BC89C58A0DB72675FBC47FEE9FF33C16ADE6D341936B06B6A6F5EF6F66A4EDD981DF75DA8399C3053F430ECA342437C23AF423A211AC9F58EAF09B0F837DE9D86C7109DB1646561AA5AF0289AF5514AC64BC2D9D36A179BB8A7971E2BFA03A9E4B847FD3D63524D43A0E8003547B94A8A75E519DF3177D0A60BC0B4BAB1EA59A2CBB4D2D62354E926E9C7D3BE4181E81BA60F8285A896D17DA8C3242481B6C405769A39D547C74ED9FF95A70A796046B5EFF36682DC29DF0314C0D15F6CD957E491DB56DCDD1CA87A03EBE06B7BDF040103DF05083230323631323331DF060101DF070101\n"
                                +"9F0605A0000000049F220103DF028180C2490747FE17EB0584C88D47B1602704150ADC88C5B998BD59CE043EDEBF0FFEE3093AC7956AD3B6AD4554C6DE19A178D6DA295BE15D5220645E3C8131666FA4BE5B84FE131EA44B039307638B9E74A8C42564F892A64DF1CB15712B736E3374F1BBB6819371602D8970E97B900793C7C2A89A4A1649A59BE680574DD0B60145DF03145ADDF21D09278661141179CBEFF272EA384B13BBDF040103DF05083230303931323331DF060101DF070101\n"
                                +"9F0605A0000000049F220104DF028190A6DA428387A502D7DDFB7A74D3F412BE762627197B25435B7A81716A700157DDD06F7CC99D6CA28C2470527E2C03616B9C59217357C2674F583B3BA5C7DCF2838692D023E3562420B4615C439CA97C44DC9A249CFCE7B3BFB22F68228C3AF13329AA4A613CF8DD853502373D62E49AB256D2BC17120E54AEDCED6D96A4287ACC5C04677D4A5A320DB8BEE2F775E5FEC5DF0314381A035DA58B482EE2AF75F4C3F2CA469BA4AA6CDF040103DF05083230313731323331DF060101DF070101\n"
                                +"9F0605A0000000049F220105DF0281B0B8048ABC30C90D976336543E3FD7091C8FE4800DF820ED55E7E94813ED00555B573FECA3D84AF6131A651D66CFF4284FB13B635EDD0EE40176D8BF04B7FD1C7BACF9AC7327DFAA8AA72D10DB3B8E70B2DDD811CB4196525EA386ACC33C0D9D4575916469C4E4F53E8E1C912CC618CB22DDE7C3568E90022E6BBA770202E4522A2DD623D180E215BD1D1507FE3DC90CA310D27B3EFCCD8F83DE3052CAD1E48938C68D095AAC91B5F37E28BB49EC7ED597DF0314EBFA0D5D06D8CE702DA3EAE890701D45E274C845DF040103DF05083230323331323331DF060101DF070101\n"
                                +"9f0605a0000003719f220105df05083230323831323331df060101df070101df0281B0AEAC5DB7F879F5C0DAB6CE358CBD6AA4213D13E7635BE3886CCE9304161D7A9E9FF3C26412957A8318C90602946A297BF5235F7FDD4137B3762D69A25C0889BF839A03C6392CBB325FBC87B892E9C49E8F005F3454D29CD9C545259405E909F040CC2877B6CE9DD1E02D79224C8C3312FB1B223B26414934D0618B7E329C25039C1D98683376637804A2B26F62301EEAD2531D5D3E62BC507EF88FFB15A0101E64A6EA209A72DDA08F4ADAF04E1C52D9DF031470690c8782d517f04d09c5bfd352f6e6043f3069df040103\n"
                                +"9f0605a0000003719f220106df05083230323831323331df060101df070101df0281F8D2DA0134B4DFC93A75EE8960C99896D50A91527B87BA7B16CDB77E5B6FDB750EB70B54026CADDA1D562C77A2C6DA541E94BC415D43E68489B16980F2E887C09E4CF90E2E639B179277BBA0E982CCD1F80521D1457209125B3ABCD309E1B92B5AEDA2EB1CBF933BEAD9CE7365E52B7D17FCB405AA28E5DE6AA3F08E764F745E70859ABCBA41E570A6E4367B3D6FECE723B73ABF3EB53DCDE3816E8A813460447021509D0DFDF2EEEE74CC35485FB55C26836EB3BF9C7DEBEE6C0B77B7BE059233801CF76B321FCA25FB1E63117AE1865E23161EC39D7B1FB84256C2BE72BF8EC771548DB9F00BEF77C509FADA15E2B53FF950D383F96211D3DF0314f5bab84ece5f8bd45511e5ca861b80c7e6c51f55df040103\n"
                                +"9f0605a0000005249f220103df028190e703a908ffae3730f82e550869a294c1ff1da25f2b53d2c8bb18f770dad505135d03d5ec8ee3926550051c3d4857f6fedb882c2889e0b25f389f78741f2931a92d45d3a47e62810d3253653ab0ab3570c35dfd08d3167b6db42ed28f765186f4287cdaf9d9bad20bce2c4ecfecdd218e50f1fcc718878882f3934a6feb502cfcad615a2b2e279a0868dda9489dfa9cd9df03144b93d1e1f57cfa16970501f17d3e06411043f1d5df040103df050420311222df060101df070101\n"
                                +"9f0605a0000005249f220104df0281b0ac0019624fc0a72270c6885cc0b3c9140c351fcfe6f8145881a27750393453d3265f69e7658132d8d253edf8991e2ba32b782d39ade1ff1fc8f211f5df51a0007c761ad9882587bd6a36aecd3abbf944307ac97a2d905fab489c3e1ccd76de9eb93ecfab2bb84f34e770119e356dc6372d8685da8eb92fcac7b53c0167100e4cdfb9830d1c45e787e44c9f6a42ec131a6a4cd66bbe4f93ca91fdf157c7b22fc7221a6348f0eda6151302a80ef77d6ca5df03146f843ce765b9144ce1a6bfea46bc37b65081ce7fdf040103df050420311222df060101df070101\n"
                                +"9f0605a0000005249f220105df0281f8c04e80180369898aaef6ee7741eded25239d765301614b5b41a0096a3009358d626d828bc5f1b1e04a2dc1367101266905d262003be747fd231c9b0011f2f2b21ba8e4c0f4ca5e93ed9dbb2e92abc450576a4eb59ad00dca59c8bf3230e4b19d43452871c6215d837663310df43caea1b9b08c1f500af1b550f62e18d70eee9e9475321bcd1799ab193e0bc849dace892a0e6a1f42fe0786db30345ae1a0e7e4c4b71640e03bfd2832c491a7d83f3b4ef4d388cddbb748c2fd1d9d4a9bf52fc856cba088d4b274846002c23cda722c5cff3b1f8218a1843b0426474bdc92f2f5e31fbf321cc17480ad069df55381f2e601d5cba7b871253fdf03147081df2a0c36360f24c122c574f0ad2e57893dd2df040103df050420311222df060101df070101\n"
                                +"9f0605a0000000259f220103df040103df060101df070101df03148708a3e3bbc1bb0be73ebd8d19d4e5d20166bf6cdf028180b0c2c6e2a6386933cd17c239496bf48c57e389164f2a96bff133439ae8a77b20498bd4dc6959ab0c2d05d0723af3668901937b674e5a2fa92ddd5e78ea9d75d79620173cc269b35f463b3d4aaff2794f92e6c7a3fb95325d8ab95960c3066be548087bcb6ce12688144a8b4a66228ae4659c634c99e36011584c095082a3a3e3\n"
                                +"9f0605a0000000259f22010edf040103df060101df070101df0314a7266abae64b42a3668851191d49856e17f8fbcddf028190aa94a8c6dad24f9ba56a27c09b01020819568b81a026be9fd0a3416ca9a71166ed5084ed91ced47dd457db7e6cbcd53e560bc5df48abc380993b6d549f5196cfa77dfb20a0296188e969a2772e8c4141665f8bb2516ba2c7b5fc91f8da04e8d512eb0f6411516fb86fc021ce7e969da94d33937909a53a57f907c40c22009da7532cb3be509ae173b39ad6a01ba5bb85\n"
                                +"9f0605a0000000259f22010fdf040103df060101df070101df0314a73472b3ab557493a9bc2179cc8014053b12bab4df0281b0c8d5ac27a5e1fb89978c7c6479af993ab3800eb243996fbb2ae26b67b23ac482c4b746005a51afa7d2d83e894f591a2357b30f85b85627ff15da12290f70f05766552ba11ad34b7109fa49de29dcb0109670875a17ea95549e92347b948aa1f045756de56b707e3863e59a6cbe99c1272ef65fb66cbb4cff070f36029dd76218b21242645b51ca752af37e70be1a84ff31079dc0048e928883ec4fadd497a719385c2bbbebc5a66aa5e5655d18034ec5\n"
                                +"9f0605a0000000259f220110df040103df060101df070101df0314c729cf2fd262394abc4cc173506502446aa9b9fddf0281f8cf98dfedb3d3727965ee7797723355e0751c81d2d3df4d18ebab9fb9d49f38c8c4a826b99dc9dea3f01043d4bf22ac3550e2962a59639b1332156422f788b9c16d40135efd1ba94147750575e636b6ebc618734c91c1d1bf3edc2a46a43901668e0ffc136774080e888044f6a1e65dc9aaa8928dacbeb0db55ea3514686c6a732cef55ee27cf877f110652694a0e3484c855d882ae191674e25c296205bbb599455176fdd7bbc549f27ba5fe35336f7e29e68d783973199436633c67ee5a680f05160ed12d1665ec83d1997f10fd05bbdbf9433e8f797aee3e9f02a34228ace927abe62b8b9281ad08d3df5c7379685045d7ba5fcde58637\n"
                                +"9f0605a0000000259f220167df040103df060101df070101df031452a2907300c8445bf54b970c894691feadf2d28edf0281b0c687adccf3d57d3360b174e471eda693aa555dfdc6c8cd394c74ba25ccdf8eabfd1f1ceadfbe2280c9e81f7a058998dc22b7f22576fe84713d0bdd3d34cfcd12fcd0d26901ba74103d075c664dabccaf57bf789494051c5ec303a2e1d784306d3db3eb665cd360a558f40b7c05c919b2f0282fe1ed9bf6261aa814648fbc263b14214491de426d242d65cd1fff0fbe4d4daff5cfacb2adc7131c9b147ee791956551076270696b75fd97373f1fd7804f\n"
                                ;
                List<String> capkList = new ArrayList<>(Arrays.asList(strCapk.split("\n")));
                try {
                    String strpackageName = getPackageName();
                    int ret = mEmvBinderService.saveCapk(capkList, strpackageName);
                    if (ret == 0 ){
                        tv_addCAPKresult.setText("save all capk is success!");
                    }else {
                        tv_addCAPKresult.setText("save all capk is error!");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                Log.e("szaf", "onClick: " + capkList);
                break;

            case R.id.btn_add_allCAPKoffine:
                String strCapkOffline =
                        "9F0605A0000003339F220101DF028180BBE9066D2517511D239C7BFA77884144AE20C7372F515147E8CE6537C54C0A6A4D45F8CA4D290870CDA59F1344EF71D17D3F35D92F3F06778D0D511EC2A7DC4FFEADF4FB1253CE37A7B2B5A3741227BEF72524DA7A2B7B1CB426BEE27BC513B0CB11AB99BC1BC61DF5AC6CC4D831D0848788CD74F6D543AD37C5A2B4C5D5A93BDF0314E881E390675D44C2DD81234DCE29C3F5AB2297A0DF040103DF05083230303931323331DF060101DF070101\n"
                                +"9F0605A0000003339F220102DF028190A3767ABD1B6AA69D7F3FBF28C092DE9ED1E658BA5F0909AF7A1CCD907373B7210FDEB16287BA8E78E1529F443976FD27F991EC67D95E5F4E96B127CAB2396A94D6E45CDA44CA4C4867570D6B07542F8D4BF9FF97975DB9891515E66F525D2B3CBEB6D662BFB6C3F338E93B02142BFC44173A3764C56AADD202075B26DC2F9F7D7AE74BD7D00FD05EE430032663D27A57DF031403BB335A8549A03B87AB089D006F60852E4B8060DF040103DF05083230323131323331DF060101DF070101\n"
                                +"9F0605A0000003339F220103DF0281B0B0627DEE87864F9C18C13B9A1F025448BF13C58380C91F4CEBA9F9BCB214FF8414E9B59D6ABA10F941C7331768F47B2127907D857FA39AAF8CE02045DD01619D689EE731C551159BE7EB2D51A372FF56B556E5CB2FDE36E23073A44CA215D6C26CA68847B388E39520E0026E62294B557D6470440CA0AEFC9438C923AEC9B2098D6D3A1AF5E8B1DE36F4B53040109D89B77CAFAF70C26C601ABDF59EEC0FDC8A99089140CD2E817E335175B03B7AA33DDF031487F0CD7C0E86F38F89A66F8C47071A8B88586F26DF040103DF05083230323431323331DF060101DF070101\n"
                               ;
                List<String> capkListOffline = new ArrayList<>(Arrays.asList(strCapkOffline.split("\n")));
                try {
                    String strpackageName = getPackageName();
                    int ret = mEmvBinderService.saveCapk(capkListOffline, "com.hjq.demo");
                    if (ret == 0 ){
                        tv_addCAPKresultoffline.setText("save all capk is success!");
                    }else {
                        tv_addCAPKresultoffline.setText("save all capk is error!");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                Log.e("szaf", "onClick: " + capkListOffline);
                break;
 // Delete all CAPKs CAPK
            case R.id.btn_delALLCAPK:
                try {
                    int ret = mEmvBinderService.delAllCapk(getPackageName());
                    if (ret == 0){
                        tv_delALLCAPK.setText("del the all Capk is success!");
                    }else {
                        tv_delALLCAPK.setText("del the all Capk is error!");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
 // Example Query a CAPK CAPK
            case R.id.btnQueryCAPK:
                try {
                    String packageName = getPackageName();
                    String getStrCapk = mEmvBinderService.queryCapk(packageName);

                    if (TextUtils.isEmpty(getStrCapk)){
                        Toast.makeText(CapkActivity.this, "Capk is not written. Please write it first", Toast.LENGTH_SHORT)
                                .show();
                    }else {
                        Gson gson = new Gson();
                        List<CapkEntity> studentInfoList = gson.fromJson(getStrCapk,
                                new TypeToken<List<CapkEntity>>() {}.getType());
                        String[] serviceItemName = new String[studentInfoList.size()];
                        for (int i = 0; i < studentInfoList.size(); i++) {
                            StringBuffer stringBuffer = new StringBuffer();
                            stringBuffer.append(studentInfoList.get(i).getTag9F06()+","+studentInfoList.get(i).getTag9F22());
                            serviceItemName[i] = stringBuffer.toString() ;
                        }
                        ListView testListView = (ListView) findViewById(R.id.lv_main);
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                                android.R.layout.simple_expandable_list_item_1, serviceItemName);
                        testListView.setAdapter(adapter);
                        testListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 // todo CAPK
                                try {

                                    String ssss= serviceItemName[position];

                                    String[] capkValue=ssss.split(",");

                                    int ret = mEmvBinderService.delCapk(getPackageName(),capkValue[0],capkValue[1]);
                                    if (ret == 0 ){
                                        Toast.makeText(CapkActivity.this, "del Capk："+serviceItemName[position]+"success!", Toast.LENGTH_SHORT)
                                                .show();
                                    }else {
                                        Toast.makeText(CapkActivity.this, "del Capk："+serviceItemName[position]+"error!", Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                break;

            case R.id.btnQueryCAPKOffline:
                try {
                    String packageName = getPackageName();
                    String getStrCapk = mEmvBinderService.queryCapk("com.hjq.demo");

                    if (TextUtils.isEmpty(getStrCapk)){
                        Toast.makeText(CapkActivity.this, "Capk is not written. Please write it first", Toast.LENGTH_SHORT)
                                .show();
                    }else {
                        Gson gson = new Gson();
                        List<CapkEntity> studentInfoList = gson.fromJson(getStrCapk,
                                new TypeToken<List<CapkEntity>>() {}.getType());
                        String[] serviceItemName = new String[studentInfoList.size()];
                        for (int i = 0; i < studentInfoList.size(); i++) {
                            StringBuffer stringBuffer = new StringBuffer();
                            stringBuffer.append(studentInfoList.get(i).getTag9F06()+","+studentInfoList.get(i).getTag9F22());
                            serviceItemName[i] = stringBuffer.toString() ;
                        }
                        ListView testListView = (ListView) findViewById(R.id.lv_main);
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                                android.R.layout.simple_expandable_list_item_1, serviceItemName);
                        testListView.setAdapter(adapter);
                        testListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 // todo CAPK
                                try {

                                    String ssss= serviceItemName[position];

                                    String[] capkValue=ssss.split(",");

                                    int ret = mEmvBinderService.delCapk(getPackageName(),capkValue[0],capkValue[1]);
                                    if (ret == 0 ){
                                        Toast.makeText(CapkActivity.this, "del Capk："+serviceItemName[position]+"success!", Toast.LENGTH_SHORT)
                                                .show();
                                    }else {
                                        Toast.makeText(CapkActivity.this, "del Capk："+serviceItemName[position]+"error!", Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                break;



        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WatermarkUtils.removeWatermark(this);
    }
}
