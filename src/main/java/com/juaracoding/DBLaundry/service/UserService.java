package com.juaracoding.DBLaundry.service;


import com.juaracoding.DBLaundry.configuration.OtherConfig;
import com.juaracoding.DBLaundry.core.BcryptImpl;
import com.juaracoding.DBLaundry.dto.ForgetPasswordDTO;
import com.juaracoding.DBLaundry.dto.UserDTO;
import com.juaracoding.DBLaundry.handler.ResourceNotFoundException;
import com.juaracoding.DBLaundry.handler.ResponseHandler;
import com.juaracoding.DBLaundry.model.Akses;
import com.juaracoding.DBLaundry.model.Userz;
import com.juaracoding.DBLaundry.repo.UserRepo;
import com.juaracoding.DBLaundry.utils.ConstantMessage;
import com.juaracoding.DBLaundry.utils.ExecuteSMTP;
import com.juaracoding.DBLaundry.utils.LoggingFile;
import com.juaracoding.DBLaundry.utils.TransformToDTO;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@Transactional
public class UserService {

    private UserRepo userRepo;

    private String [] strExceptionArr = new String[2];
    @Autowired
    private ModelMapper modelMapper;

    private String[] strProfile = new String[3];

    private TransformToDTO transformToDTO = new TransformToDTO();
    private Map<String,String> mapColumnSearch = new HashMap<String,String>();
    private Map<String,Object> objectMapper = new HashMap<String,Object>();

    private StringBuilder stringBuilder = new StringBuilder();

    @Autowired
    public UserService(UserRepo userService) {
        mapColumn();
        strExceptionArr[0] = "UserService";
        this.userRepo = userService;
    }
    private void mapColumn()
    {
        mapColumnSearch.put("id","ID USER");
        mapColumnSearch.put("nama","NAMA LENGKAP");
        mapColumnSearch.put("email","EMAIL");
        mapColumnSearch.put("noHP","NO HP");
    }

    public Map<String,Object> checkRegis(Userz userz, WebRequest request) {
        int intVerification = new Random().nextInt(100000,999999);
        List<Userz> listUserResult = userRepo.findByEmailOrNoHPOrUsername(userz.getEmail(),userz.getNoHP(),userz.getUsername());//INI VALIDASI USER IS EXISTS
        String emailForSMTP = userz.getEmail();
        try
        {



            if(listUserResult.size()!=0)//kondisi mengecek apakah user terdaftar
            {

                emailForSMTP = userz.getEmail();
                Userz nextUser = listUserResult.get(0);
                if(nextUser.getIsDelete()!=0)//sudah terdaftar dan aktif
                {
                    //PEMBERITAHUAN SAAT REGISTRASI BAGIAN MANA YANG SUDAH TERDAFTAR (USERNAME, EMAIL ATAU NOHP)
                    if(nextUser.getEmail().equals(userz.getEmail()))
                    {
                        return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_EMAIL_ISEXIST,
                                HttpStatus.NOT_ACCEPTABLE,null,"FV01001",request);//EMAIL SUDAH TERDAFTAR DAN AKTIF
                    } else if (nextUser.getNoHP().equals(userz.getNoHP())) {//FV = FAILED VALIDATION
                        return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_NOHP_ISEXIST,
                                HttpStatus.NOT_ACCEPTABLE,null,"FV01002",request);//NO HP SUDAH TERDAFTAR DAN AKTIF
                    } else if (nextUser.getUsername().equals(userz.getUsername())) {
                        return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_USERNAME_ISEXIST,
                                HttpStatus.NOT_ACCEPTABLE,null,"FV01003",request);//USERNAME SUDAH TERDAFTAR DAN AKTIF
                    } else {
                        return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_USER_ISACTIVE,
                                HttpStatus.NOT_ACCEPTABLE,null,"FV01004",request);//KARENA YANG DIAMBIL DATA YANG PERTAMA JADI ANGGAPAN NYA SUDAH TERDAFTAR SAJA
                    }
                }
                else
                {
                    nextUser.setPassword(BcryptImpl.hash(userz.getPassword()+userz.getUsername()));
                    nextUser.setToken(BcryptImpl.hash(String.valueOf(intVerification)));
                    nextUser.setTokenCounter(nextUser.getTokenCounter()+1);//setiap kali mencoba ditambah 1
                    nextUser.setModifiedBy(Integer.parseInt(nextUser.getIdUser().toString()));
                    nextUser.setModifiedDate(new Date());
                }
            }
            else//belum terdaftar
            {
                userz.setPassword(BcryptImpl.hash(userz.getPassword()+userz.getUsername()));
                userz.setToken(BcryptImpl.hash(String.valueOf(intVerification)));
                userRepo.save(userz);
            }

            strProfile[0]="TOKEN UNTUK VERIFIKASI EMAIL";
            strProfile[1]=userz.getNamaLengkap();
            strProfile[2]=String.valueOf(intVerification);

            /*EMAIL NOTIFICATION*/
            if(OtherConfig.getFlagSMTPActive().equalsIgnoreCase("y") && !emailForSMTP.equals(""))
            {
                new ExecuteSMTP().sendSMTPToken(emailForSMTP,"VERIFIKASI TOKEN REGISTRASI",
                        "TOKEN UNTUK VERIFIKASI EMAIL",strProfile,"\\data\\ver_regis.html");
            }
            System.out.println("VERIFIKASI -> "+intVerification);
        }catch (Exception e)
        {
            strExceptionArr[1]="checkRegis(Userz userz) --- LINE 39";
            LoggingFile.exceptionStringz(strExceptionArr,e, OtherConfig.getFlagLogging());
            return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_FLOW_INVALID,
                    HttpStatus.NOT_FOUND,null,"FE01001",request);
        }
        return new ResponseHandler().generateModelAttribut(ConstantMessage.SUCCESS_CHECK_REGIS,
                HttpStatus.CREATED,null,null,request);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> confirmRegis(Userz userz, String emails, WebRequest request) {
        List<Userz> listUserResult = userRepo.findByEmail(emails);
        try
        {
            if(listUserResult.size()!=0)
            {
                Userz nextUser = listUserResult.get(0);
                if(!BcryptImpl.verifyHash(userz.getToken(),nextUser.getToken()))
                {
                    return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_TOKEN_INVALID,
                            HttpStatus.NOT_ACCEPTABLE,null,"FV01005",request);
                }
                nextUser.setIsDelete((byte) 1);//SET REGISTRASI BERHASIL
                Akses akses = new Akses();
                akses.setIdAkses(1L);
                nextUser.setAkses(akses);
            }
            else
            {
                return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_USER_NOT_EXISTS,
                    HttpStatus.NOT_FOUND,null,"FV01006",request);
            }
        }
        catch (Exception e)
        {
            strExceptionArr[1]="confirmRegis(Userz userz)  --- LINE 105";
            LoggingFile.exceptionStringz(strExceptionArr,e, OtherConfig.getFlagLogging());
            return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_FLOW_INVALID,
                    HttpStatus.INTERNAL_SERVER_ERROR,null,"FE01002",request);
        }

        return new ResponseHandler().generateModelAttribut(ConstantMessage.SUCCESS_CHECK_REGIS,
                HttpStatus.OK,null,null,request);
    }
    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> doLogin(Userz userz, WebRequest request) {
        userz.setUsername(userz.getEmail());
        userz.setNoHP(userz.getNoHP());
        List<Userz> listUserResult = userRepo.findByEmailOrNoHPOrUsername(userz.getEmail(),userz.getNoHP(),userz.getUsername());//DATANYA PASTI HANYA 1
        Userz nextUser = null;
        try
        {
            if(listUserResult.size()!=0)
            {
                nextUser = listUserResult.get(0);
                if(!BcryptImpl.verifyHash(userz.getPassword()+nextUser.getUsername(),nextUser.getPassword()))//dicombo dengan userName
                {
                    return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_LOGIN_FAILED,
                            HttpStatus.NOT_ACCEPTABLE,null,"FV01007",request);
                }
                nextUser.setLastLoginDate(new Date());
                nextUser.setTokenCounter(0);//SETIAP KALI LOGIN BERHASIL , BERAPA KALIPUN UJI COBA REQUEST TOKEN YANG SEBELUMNYA GAGAL AKAN SECARA OTOMATIS DIRESET MENJADI 0
                nextUser.setPasswordCounter(0);//SETIAP KALI LOGIN BERHASIL , BERAPA KALIPUN UJI COBA YANG SEBELUMNYA GAGAL AKAN SECARA OTOMATIS DIRESET MENJADI 0
            }
            else
            {
                return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_USER_NOT_EXISTS,
                        HttpStatus.NOT_ACCEPTABLE,null,"FV01008",request);
            }
        }

        catch (Exception e)
        {
            strExceptionArr[1]="doLogin(Userz userz,WebRequest request)  --- LINE 140";
            LoggingFile.exceptionStringz(strExceptionArr,e, OtherConfig.getFlagLogging());
            return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_LOGIN_FAILED,
                    HttpStatus.INTERNAL_SERVER_ERROR,null,"FE01003",request);
        }

        return new ResponseHandler().generateModelAttribut(ConstantMessage.SUCCESS_LOGIN,
                HttpStatus.OK,nextUser,null,request);
    }

    public Map<String,Object> getNewToken(String emailz, WebRequest request) {
        List<Userz> listUserResult = userRepo.findByEmail(emailz);//DATANYA PASTI HANYA 1
        String emailForSMTP = "";
        int intVerification = 0;
        Userz userz = null;
        try
        {
            if(listUserResult.size()!=0)
            {
                intVerification = new Random().nextInt(100000,999999);
                Userz users = listUserResult.get(0);
                users.setToken(BcryptImpl.hash(String.valueOf(intVerification)));
                users.setModifiedDate(new Date());
                users.setModifiedBy(Integer.parseInt(users.getIdUser().toString()));
                System.out.println("New Token -> "+intVerification);
                emailForSMTP = users.getEmail();
            }
            else
            {
                return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_FLOW_INVALID,
                        HttpStatus.NOT_ACCEPTABLE,null,"FV01009",request);
            }
        }
        catch (Exception e)
        {
            strExceptionArr[1]="getNewToken(String emailz, WebRequest request)  --- LINE 178";
            LoggingFile.exceptionStringz(strExceptionArr,e, OtherConfig.getFlagLogging());
            return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_FLOW_INVALID,
                    HttpStatus.INTERNAL_SERVER_ERROR,null,"FE01004",request);
        }

        /*
                call method send SMTP
         */

        strProfile[0]="TOKEN BARU UNTUK VERIFIKASI GANTI PASSWORD";
        strProfile[1]=userz.getNamaLengkap();
        strProfile[2]=String.valueOf(intVerification);

        /*EMAIL NOTIFICATION*/
        if(OtherConfig.getFlagSMTPActive().equalsIgnoreCase("y") && !emailForSMTP.equals(""))
        {
            if(OtherConfig.getFlagSMTPActive().equalsIgnoreCase("y") && !emailForSMTP.equals(""))
            {
                new ExecuteSMTP().sendSMTPToken(emailForSMTP,"VERIFIKASI TOKEN REGISTRASI",
                        "TOKEN UNTUK VERIFIKASI EMAIL",strProfile,"\\data\\ver_token_baru.html");
            }
        }

        return new ResponseHandler().generateModelAttribut(ConstantMessage.SUCCESS_LOGIN,
                HttpStatus.OK,null,null,request);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> sendMailForgetPwd(String email,WebRequest request)
    {
        int intVerification =0;
        List<Userz> listUserResults = userRepo.findByEmail(email);
        Userz userz = null;
        try
        {
            if(listUserResults.size()==0)
            {
                return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_USER_NOT_EXISTS,
                        HttpStatus.NOT_FOUND,null,"FV01010",request);
            }
            intVerification = new Random().nextInt(100000,999999);
            Userz users = listUserResults.get(0);
            users.setToken(BcryptImpl.hash(String.valueOf(intVerification)));
            users.setModifiedDate(new Date());
            users.setModifiedBy(Integer.parseInt(users.getIdUser().toString()));
            System.out.println("New Forget Password Token -> "+intVerification);
        }
        catch (Exception e)
        {
            strExceptionArr[1]="sendMailForgetPwd(String email)  --- LINE 223";
            LoggingFile.exceptionStringz(strExceptionArr,e, OtherConfig.getFlagLogging());
            return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_FLOW_INVALID,
                    HttpStatus.INTERNAL_SERVER_ERROR,null,"FE01005",request);
        }

        strProfile[0]="TOKEN UNTUK VERIFIKASI LUPA PASSWORD";
        strProfile[1]=userz.getNamaLengkap();
        strProfile[2]=String.valueOf(intVerification);

        /*EMAIL NOTIFICATION*/
        if(OtherConfig.getFlagSMTPActive().equalsIgnoreCase("y") && !userz.getEmail().equals(""))
        {
            new ExecuteSMTP().sendSMTPToken(userz.getEmail(),"VERIFIKASI TOKEN REGISTRASI",
                    "TOKEN UNTUK VERIFIKASI LUPA PASSWORD",strProfile,"\\data\\ver_lupa_pwd.html");
        }
        return new ResponseHandler().generateModelAttribut(ConstantMessage.SUCCESS_SEND_NEW_TOKEN,
                HttpStatus.OK,null,null,request);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> confirmTokenForgotPwd(ForgetPasswordDTO forgetPasswordDTO, WebRequest request)
    {
        String emailz = forgetPasswordDTO.getEmail();
        String token = forgetPasswordDTO.getToken();

        List<Userz> listUserResults = userRepo.findByEmail(emailz);
        try
        {
            if(listUserResults.size()==0)
            {
                return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_USER_NOT_EXISTS,
                        HttpStatus.NOT_FOUND,null,"FV01011",request);
            }

            Userz userz = listUserResults.get(0);

            if(!BcryptImpl.verifyHash(token,userz.getToken()))//VALIDASI TOKEN
            {
                return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_TOKEN_FORGOTPWD_NOT_SAME,
                        HttpStatus.NOT_FOUND,null,"FV01012",request);
            }
        }
        catch (Exception e)
        {
            strExceptionArr[1]="confirmTokenForgotPwd(ForgetPasswordDTO forgetPasswordDTO, WebRequest request)  --- LINE 262";
            LoggingFile.exceptionStringz(strExceptionArr,e, OtherConfig.getFlagLogging());
            return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_FLOW_INVALID,
                    HttpStatus.INTERNAL_SERVER_ERROR,null,"FE01006",request);
        }
        return new ResponseHandler().generateModelAttribut(ConstantMessage.SUCCESS_TOKEN_MATCH,
                HttpStatus.OK,null,null,request);
    }


    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> confirmPasswordChange(ForgetPasswordDTO forgetPasswordDTO, WebRequest request)
    {
        String emailz = forgetPasswordDTO.getEmail();
        String newPassword = forgetPasswordDTO.getNewPassword();
        String oldPassword = forgetPasswordDTO.getOldPassword();
        String confirmPassword = forgetPasswordDTO.getConfirmPassword();

        List<Userz> listUserResults = userRepo.findByEmail(emailz);
        try
        {
            if(listUserResults.size()==0)
            {
                return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_FLOW_INVALID,
                        HttpStatus.NOT_FOUND,null,"FV01012",request);
            }

            Userz userz = listUserResults.get(0);
            if(!BcryptImpl.verifyHash(oldPassword+userz.getUsername(),userz.getPassword()))//kalau password lama tidak sama dengan yang diinput
            {
                return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_PASSWORD_NOT_SAME,
                        HttpStatus.NOT_FOUND,null,"FV01013",request);
            }
            if(oldPassword.equals(newPassword))//PASSWORD BARU SAMA DENGAN PASSWORD LAMA
            {
                return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_PASSWORD_IS_SAME,
                        HttpStatus.NOT_FOUND,null,"FV01014",request);
            }
            if(!confirmPassword.equals(newPassword))//PASSWORD BARU DENGAN PASSWORD KONFIRMASI TIDAK SAMA
            {
                return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_PASSWORD_CONFIRM_FAILED,
                        HttpStatus.NOT_FOUND,null,"FV01014",request);
            }

            userz.setPassword(BcryptImpl.hash(String.valueOf(newPassword+userz.getUsername())));
            userz.setIsDelete((byte)1);
            userz.setModifiedDate(new Date());
            userz.setModifiedBy(Integer.parseInt(userz.getIdUser().toString()));
            System.out.println("New Forget Password -> "+newPassword);
        }

        catch (Exception e)
        {
            strExceptionArr[1]="confirmPasswordChange(ForgetPasswordDTO forgetPasswordDTO, WebRequest request)  --- LINE 297";
            LoggingFile.exceptionStringz(strExceptionArr,e, OtherConfig.getFlagLogging());
            return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_FLOW_INVALID,
                    HttpStatus.INTERNAL_SERVER_ERROR,null,"FE01006",request);
        }
        return new ResponseHandler().generateModelAttribut(ConstantMessage.SUCCESS_CHANGE_PWD,
                HttpStatus.OK,null,null,request);
    }


    public Map<String, Object> saveUser(Userz userz, WebRequest request) {
        String strMessage = ConstantMessage.SUCCESS_SAVE;
        Object strUserIdz = request.getAttribute("USR_ID",1);
        int intVerification = new Random().nextInt(100000,999999);
        String strToken = "";
        try {
            if(strUserIdz==null)
            {
                return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_FLOW_INVALID,
                        HttpStatus.NOT_ACCEPTABLE,null,"FV03001",request);
            }
            strToken = BcryptImpl.hash(String.valueOf(intVerification));
            userz.setPassword(strToken);
            userz.setToken(String.valueOf(intVerification));
            userz.setCreatedBy(Integer.parseInt(strUserIdz.toString()));
            userz.setAkses(userz.getAkses());
            userz.setCreatedDate(new Date());
            userRepo.save(userz);
        } catch (Exception e) {
            strExceptionArr[1] = "saveUser(Userz userz, WebRequest request) --- LINE 67";
            LoggingFile.exceptionStringz(strExceptionArr, e, OtherConfig.getFlagLogging());
            return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_SAVE_FAILED,
                    HttpStatus.BAD_REQUEST,
                    transformToDTO.transformObjectDataEmpty(objectMapper,mapColumnSearch),
                    "FE03001", request);
        }

        strProfile[0]="LINK SET PASSWORD";
        strProfile[1]=userz.getNamaLengkap();
        stringBuilder.setLength(0);
        strProfile[2]=stringBuilder.append(OtherConfig.getUrlEndPointVerify())
                .append("/api/authz/v1/userman/vermail?uid=").append(BcryptImpl.hash(userz.getUsername())).
                append("&tkn=").append(strToken).
                append("&mail=").append(userz.getEmail()).toString();



        /*EMAIL NOTIFICATION*/
        if(OtherConfig.getFlagSMTPActive().equalsIgnoreCase("y") && !userz.getEmail().equals(""))
        {
            new ExecuteSMTP().sendSMTPToken(userz.getEmail(),"AKUN TELAH DIBUAT",
                    "LINK SET PASSWORD",strProfile,"\\data\\ver_set_pwd.html");
        }

        return new ResponseHandler().generateModelAttribut(strMessage,
                HttpStatus.CREATED,
                transformToDTO.transformObjectDataSave(objectMapper, userz.getIdUser(),mapColumnSearch),
                null, request);
    }

    public Map<String, Object> updateUser(Long idUser, Userz userz, WebRequest request) {
        String strMessage = ConstantMessage.SUCCESS_UPDATE;
        Object strUserIdz = request.getAttribute("USR_ID",1);
        try {
            Userz nextUserz = userRepo.findById(idUser).orElseThrow(
                    ()->null
            );

            if(nextUserz==null)
            {
                return new ResponseHandler().generateModelAttribut(ConstantMessage.WARNING_MENU_NOT_EXISTS,
                        HttpStatus.NOT_ACCEPTABLE,
                        transformToDTO.transformObjectDataEmpty(objectMapper,mapColumnSearch),
                        "FV01002",request);
            }
            if(strUserIdz==null)
            {
                return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_FLOW_INVALID,
                        HttpStatus.NOT_ACCEPTABLE,
                        null,
                        "FV03003",request);
            }
            nextUserz.setNamaLengkap(userz.getNamaLengkap());
            nextUserz.setPassword(BcryptImpl.hash(userz.getPassword()+userz.getUsername()));
            nextUserz.setTanggalLahir(userz.getTanggalLahir());
            nextUserz.setEmail(userz.getEmail());
            nextUserz.setAkses(userz.getAkses());
            nextUserz.setNoHP(userz.getNoHP());
            nextUserz.setUsername(userz.getUsername());
            nextUserz.setCreatedBy(Integer.parseInt(strUserIdz.toString()));
            nextUserz.setCreatedDate(new Date());

        } catch (Exception e) {
            strExceptionArr[1] = "updateUser(Long idUser, Userz userz, WebRequest request) --- LINE 92";
            LoggingFile.exceptionStringz(strExceptionArr, e, OtherConfig.getFlagLogging());
            return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_SAVE_FAILED,
                    HttpStatus.BAD_REQUEST,
                    transformToDTO.transformObjectDataEmpty(objectMapper,mapColumnSearch),
                    "FE03002", request);
        }
        return new ResponseHandler().generateModelAttribut(strMessage,
                HttpStatus.CREATED,
                transformToDTO.transformObjectDataEmpty(objectMapper,mapColumnSearch),
                null, request);
    }



    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> saveUploadFileUser(List<Userz> listUserz,
                                                  MultipartFile multipartFile,
                                                  WebRequest request) throws Exception {
        List<Userz> listUserResults = null;
        String strMessage = ConstantMessage.SUCCESS_SAVE;

        try {
            listUserResults = userRepo.saveAll(listUserz);
            if (listUserResults.size() == 0) {
                strExceptionArr[1] = "saveUploadFileUser(List<Userz> listUserz, MultipartFile multipartFile, WebRequest request)  --- LINE 82";
                LoggingFile.exceptionStringz(strExceptionArr, new ResourceNotFoundException("FILE KOSONG"), OtherConfig.getFlagLogging());
                return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_EMPTY_FILE + " -- " + multipartFile.getOriginalFilename(),
                        HttpStatus.BAD_REQUEST, null, "FV03004", request);
            }
        } catch (Exception e) {
            strExceptionArr[1] = "saveUploadFileUser(List<Userz> listUserz, MultipartFile multipartFile, WebRequest request)--- LINE 88";
            LoggingFile.exceptionStringz(strExceptionArr, e, OtherConfig.getFlagLogging());
            return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_SAVE_FAILED,
                    HttpStatus.BAD_REQUEST, null, "FE03002", request);
        }
        return new ResponseHandler().
                generateModelAttribut(strMessage,
                        HttpStatus.CREATED,
                        transformToDTO.transformObjectDataEmpty(objectMapper,mapColumnSearch),
                        null,
                        request);
    }

    public Map<String,Object> findAllUser(Pageable pageable, WebRequest request)
    {
        List<UserDTO> listUserDTO = null;
        Map<String,Object> mapResult = null;
        Page<Userz> pageUser = null;
        List<Userz> listUser = null;

        try
        {
            pageUser = userRepo.findByIsDelete(pageable,(byte)1);
            listUser = pageUser.getContent();
            if(listUser.size()==0)
            {
                return new ResponseHandler().
                        generateModelAttribut(ConstantMessage.WARNING_DATA_EMPTY,
                                HttpStatus.OK,
                                transformToDTO.transformObjectDataEmpty(objectMapper,pageable,mapColumnSearch),//HANDLE NILAI PENCARIAN
                                "FV03005",
                                request);
            }
            listUserDTO = modelMapper.map(listUser, new TypeToken<List<UserDTO>>() {}.getType());
            mapResult = transformToDTO.transformObject(objectMapper,listUserDTO,pageUser,mapColumnSearch);

        }
        catch (Exception e)
        {
            strExceptionArr[1] = "findAllUser(Pageable pageable, WebRequest request) --- LINE 178";
            LoggingFile.exceptionStringz(strExceptionArr, e, OtherConfig.getFlagLogging());
            return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_INTERNAL_SERVER,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    transformToDTO.transformObjectDataEmpty(objectMapper,pageable,mapColumnSearch),//HANDLE NILAI PENCARIAN
                    "FE03003", request);
        }

        return new ResponseHandler().
                generateModelAttribut(ConstantMessage.SUCCESS_FIND_BY,
                        HttpStatus.OK,
                        mapResult,
                        null,
                        null);
    }

    public Map<String,Object> findByPage(Pageable pageable,WebRequest request,String columFirst,String valueFirst)
    {
        Page<Userz> pageUserz = null;
        List<Userz> listUserz = null;
        List<UserDTO> listUserDTO = null;
        Map<String,Object> mapResult = null;

        try
        {
            if(columFirst.equals("id"))
            {
                try
                {
                    Long.parseLong(valueFirst);
                }
                catch (Exception e)
                {
                    strExceptionArr[1] = "findByPage(Pageable pageable,WebRequest request,String columFirst,String valueFirst) --- LINE 209";
                    LoggingFile.exceptionStringz(strExceptionArr, e, OtherConfig.getFlagLogging());
                    return new ResponseHandler().
                            generateModelAttribut(ConstantMessage.WARNING_DATA_EMPTY,
                                    HttpStatus.OK,
                                    transformToDTO.transformObjectDataEmpty(objectMapper,pageable,mapColumnSearch),//HANDLE NILAI PENCARIAN
                                    "FE03004",
                                    request);
                }
            }
            pageUserz = getDataByValue(pageable,columFirst,valueFirst);
            listUserz = pageUserz.getContent();
            if(listUserz.size()==0)
            {
                return new ResponseHandler().
                        generateModelAttribut(ConstantMessage.WARNING_DATA_EMPTY,
                                HttpStatus.OK,
                                transformToDTO.transformObjectDataEmpty(objectMapper,pageable,mapColumnSearch),//HANDLE NILAI PENCARIAN EMPTY
                                "FV03006",
                                request);
            }
            listUserDTO = modelMapper.map(listUserz, new TypeToken<List<UserDTO>>() {}.getType());
            mapResult = transformToDTO.transformObject(objectMapper,listUserDTO,pageUserz,mapColumnSearch);
            System.out.println("LIST DATA => "+listUserDTO.size());
        }

        catch (Exception e)
        {
            strExceptionArr[1] = "findByPage(Pageable pageable,WebRequest request,String columFirst,String valueFirst) --- LINE 237";
            LoggingFile.exceptionStringz(strExceptionArr, e, OtherConfig.getFlagLogging());
            return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_FLOW_INVALID,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    transformToDTO.transformObjectDataEmpty(objectMapper,pageable,mapColumnSearch),
                    "FE03005", request);
        }
        return new ResponseHandler().
                generateModelAttribut(ConstantMessage.SUCCESS_FIND_BY,
                        HttpStatus.OK,
                        mapResult,
                        null,
                        request);
    }

    public Map<String,Object> findById(Long id, WebRequest request)
    {
        Userz userz = userRepo.findById(id).orElseThrow (
                ()-> null
        );
        if(userz == null)
        {
            return new ResponseHandler().generateModelAttribut(ConstantMessage.WARNING_MENU_NOT_EXISTS,
                    HttpStatus.NOT_ACCEPTABLE,
                    transformToDTO.transformObjectDataEmpty(objectMapper,mapColumnSearch),
                    "FV03005",request);
        }
        UserDTO userDTO = modelMapper.map(userz, new TypeToken<UserDTO>() {}.getType());
        return new ResponseHandler().
                generateModelAttribut(ConstantMessage.SUCCESS_FIND_BY,
                        HttpStatus.OK,
                        userDTO,
                        null,
                        request);
    }


    public List<UserDTO> getAllUser()//KHUSUS UNTUK FORM INPUT SAJA
    {
        List<UserDTO> listUserDTO = null;
        Map<String,Object> mapResult = null;
        List<Userz> listUser = null;

        try
        {
            listUser = userRepo.findByIsDelete((byte)1);
            if(listUser.size()==0)
            {
                return new ArrayList<UserDTO>();
            }
            listUserDTO = modelMapper.map(listUser, new TypeToken<List<UserDTO>>() {}.getType());
        }
        catch (Exception e)
        {
            strExceptionArr[1] = "getAllUser() --- LINE 304";
            LoggingFile.exceptionStringz(strExceptionArr, e, OtherConfig.getFlagLogging());
            return listUserDTO;
        }
        return listUserDTO;
    }



    private Page<Userz> getDataByValue(Pageable pageable, String paramColumn, String paramValue)
    {
        if(paramValue.equals(""))
        {
            return userRepo.findByIsDelete(pageable,(byte) 1);
        }
        if(paramColumn.equals("id"))
        {
            return userRepo.findByIsDeleteAndIdUser(pageable,(byte) 1,Long.parseLong(paramValue));
        } else if (paramColumn.equals("nama")) {
            return userRepo.findByIsDeleteAndNamaLengkapContainsIgnoreCase(pageable,(byte) 1,paramValue);
        } else if (paramColumn.equals("email")) {
            return userRepo.findByIsDeleteAndEmailContainsIgnoreCase(pageable,(byte) 1,paramValue);
        } else if (paramColumn.equals("username")) {
            return userRepo.findByIsDeleteAndUsernameContainsIgnoreCase(pageable,(byte) 1,paramValue);
        }else if (paramColumn.equals("noHP")) {
            return userRepo.findByIsDeleteAndNoHPContainsIgnoreCase(pageable,(byte) 1,paramValue);
        }

        return userRepo.findByIsDelete(pageable,(byte) 1);
    }

    public Map<String,Object> linkMailVerify(String usrId, String token,String mail)
    {

        List<Userz> listUsr = userRepo.findByEmail(mail);

        if(listUsr.size()==0)
        {
            strExceptionArr[1]="linkMailVerify(String usrId, String token,String mail, WebRequest request) --- LINE 666";
            LoggingFile.exceptionStringz(strExceptionArr,new ResourceNotFoundException("Otentikasi Tidak Valid"), OtherConfig.getFlagLogging());
            return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_FLOW_INVALID,
                    HttpStatus.NOT_FOUND,null,"FE01001",null);
        }
        else
        {
            Userz userz = listUsr.get(0);
            if(userz.getIsDelete()==1)
            {
                return new ResponseHandler().generateModelAttribut(ConstantMessage.USER_IS_ACTIVE,
                        HttpStatus.NOT_FOUND,null,null,null);
            }
            else
            {
                if(BcryptImpl.verifyHash(userz.getToken(),token) && BcryptImpl.verifyHash(String.valueOf(userz.getUsername()),usrId))
                {
                    return new ResponseHandler().generateModelAttribut(ConstantMessage.VERIFY_LINK_VALID,
                            HttpStatus.CREATED,null,null,null);
                }
                else
                {
                    strExceptionArr[1]="linkMailVerify(String usrId, String token,String mail, WebRequest request) --- LINE 683";
                    LoggingFile.exceptionStringz(strExceptionArr,new ResourceNotFoundException("Otentikasi Tidak Valid"), OtherConfig.getFlagLogging());
                    return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_FLOW_INVALID,
                            HttpStatus.NOT_FOUND,null,"FE01001",null);

                }
            }
        }
    }


}