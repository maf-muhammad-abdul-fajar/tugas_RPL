package com.juaracoding.DBLaundry.service;/*
Created By IntelliJ IDEA 2022.3.2 (Ultimate Edition)
Build #IU-223.8617.56, built on January 26, 2023
@Author Syarifudin a.k.a. Muhamad Syarifuidn
Java Developer
Created on 09/03/2023 13:00
@Last Modified 09/03/2023 13:00
Version 1.1
*/

import com.juaracoding.DBLaundry.configuration.OtherConfig;
import com.juaracoding.DBLaundry.dto.PesananDTO;
import com.juaracoding.DBLaundry.handler.ResourceNotFoundException;
import com.juaracoding.DBLaundry.handler.ResponseHandler;
import com.juaracoding.DBLaundry.model.Pesanan;
import com.juaracoding.DBLaundry.repo.PesananRepo;
import com.juaracoding.DBLaundry.utils.ConstantMessage;
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

/*
KODE MODUL 12
 */
@Service
@Transactional
public class PesananService {

    private PesananRepo pesananRepo;
    private String[] strExceptionArr = new String[2];
    @Autowired
    private ModelMapper modelMapper;
    private Map<String,Object> objectMapper = new HashMap<String,Object>();
    private TransformToDTO transformToDTO = new TransformToDTO();
    private Map<String,String> mapColumnSearch = new HashMap<String,String>();
    private Map<Integer, Integer> mapItemPerPage = new HashMap<Integer, Integer>();
    private String [] strColumnSearch = new String[2];

    public PesananService(PesananRepo pesananRepo) {
        strExceptionArr[0] = "PesananService";
        mapColumn();
        this.pesananRepo = pesananRepo;
    }

    private void mapColumn()
    {
        mapColumnSearch.put("id","ID PESANAN");
        mapColumnSearch.put("nama","NAMA LENGKAP");
        mapColumnSearch.put("layanan","NAMA PAKET");
        mapColumnSearch.put("tipe","TIPE LAYANAN");
        mapColumnSearch.put("berat","BERAT");
        mapColumnSearch.put("harga","HARGA PER KILO");
        mapColumnSearch.put("total","TOTAL HARGA");
        mapColumnSearch.put("cara","CARA BAYAR");
    }

    //METHOD GET PROFIT PER MONTH
    public Integer getTotalProfit(){
        Double total = pesananRepo.calculationCurrentMonthReport();
        return total.intValue();
    }

    // METHOD SAVE PESANAN BERFUNGSI MENYIMPAN DATA PESANAN
    public Map<String, Object> savePesanan(Pesanan pesanan, WebRequest request) {
        String strMessage = ConstantMessage.SUCCESS_SAVE;
        Object strUserIdz = request.getAttribute("USR_ID",1);

        try {
            if(strUserIdz==null)
            {
                return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_FLOW_INVALID,
                        HttpStatus.NOT_ACCEPTABLE,null,"FV03001",request);
            }
            pesanan.setCreatedBy(Integer.parseInt(strUserIdz.toString()));
            pesanan.setCreatedDate(new Date());
            pesananRepo.save(pesanan);
        } catch (Exception e) {
            strExceptionArr[1] = "savePesanan(Pesanan pesanan, WebRequest request) --- LINE 55";
            LoggingFile.exceptionStringz(strExceptionArr, e, OtherConfig.getFlagLogging());
            return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_SAVE_FAILED,
                    HttpStatus.BAD_REQUEST,
                    transformToDTO.transformObjectDataEmpty(objectMapper,mapColumnSearch),
                    "FE03001", request);
        }
        return new ResponseHandler().generateModelAttribut(strMessage,
                HttpStatus.CREATED,
                transformToDTO.transformObjectDataSave(objectMapper, pesanan.getIdPesanan(),mapColumnSearch),
                null, request);
    }

    // METHOD UPDATE PESANAN BERFUNGSI UNTUK MENGUBAH DATA PESANAN
    public Map<String, Object> updatePesanan(Long idPesanan,Pesanan pesanan, WebRequest request) {
        String strMessage = ConstantMessage.SUCCESS_UPDATE;
        Object strUserIdz = request.getAttribute("USR_ID",1);

        try {
            Pesanan nextPesanan = pesananRepo.findById(idPesanan).orElseThrow(
                    ()->null
            );

            if(nextPesanan==null)
            {
                return new ResponseHandler().generateModelAttribut(ConstantMessage.WARNING_PESANAN_NOT_EXISTS,
                        HttpStatus.NOT_ACCEPTABLE,
                        transformToDTO.transformObjectDataEmpty(objectMapper,mapColumnSearch),
                        "FV03002",request);
            }
            if(strUserIdz==null)
            {
                return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_FLOW_INVALID,
                        HttpStatus.NOT_ACCEPTABLE,
                        null,
                        "FV03003",request);
            }
            nextPesanan.setBerat(pesanan.getBerat());
            nextPesanan.setPaketLayanan(pesanan.getPaketLayanan());
            nextPesanan.setPembayaran(pesanan.getPembayaran());
//            nextPesanan.setPelanggan(pesanan.getPelanggan());//INI TIDAK DIUPDATE -- BUSINESS LOGIC
            nextPesanan.setModifiedBy(Integer.parseInt(strUserIdz.toString()));
            nextPesanan.setModifiedDate(new Date());

        } catch (Exception e) {
            strExceptionArr[1] = "updatePesanan(Long idPesanan,Pesanan pesanan, WebRequest request) --- LINE 82";
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

    // METHOD SAVE UPLOAD FILE PESANAN BERFUNGSI MENYIMPAN DATA PESANAN DARI FILE CSV
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> saveUploadFilePesanan(List<Pesanan> listPesanan,
                                                     MultipartFile multipartFile,
                                                     WebRequest request) throws Exception {
        List<Pesanan> listPesananResult = null;
        String strMessage = ConstantMessage.SUCCESS_SAVE;

        try {
            listPesananResult = pesananRepo.saveAll(listPesanan);
            if (listPesananResult.size() == 0) {
                strExceptionArr[1] = "saveUploadFilePesanan(List<Pesanan> listPesanan, MultipartFile multipartFile, WebRequest request) --- LINE 129";
                LoggingFile.exceptionStringz(strExceptionArr, new ResourceNotFoundException("FILE KOSONG"), OtherConfig.getFlagLogging());
                return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_EMPTY_FILE + " -- " + multipartFile.getOriginalFilename(),
                        HttpStatus.BAD_REQUEST, null, "FV03004", request);
            }
        } catch (Exception e) {
            strExceptionArr[1] = "saveUploadFilePesanan(List<Pesanan> listPesanan, MultipartFile multipartFile, WebRequest request) --- LINE 129";
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

    // METHOD FIND ALL PESANAN BERFUNGSI MENGAMBIL SEMUA FILE PESANAN
    public Map<String,Object> findAllPesanan(Pageable pageable, WebRequest request)
    {
        List<PesananDTO> listPesananDTO = null;
        Map<String,Object> mapResult = null;
        Page<Pesanan> pagePesanan = null;
        List<Pesanan> listPesanan = null;

        try
        {
            pagePesanan = pesananRepo.findByIsDelete(pageable,(byte)1);
            listPesanan = pagePesanan.getContent();
            if(listPesanan.size()==0)
            {
                return new ResponseHandler().
                        generateModelAttribut(ConstantMessage.WARNING_DATA_EMPTY,
                                HttpStatus.OK,
                                transformToDTO.transformObjectDataEmpty(objectMapper,pageable,mapColumnSearch),//HANDLE NILAI PENCARIAN
                                "FV03005",
                                request);
            }
            listPesananDTO = modelMapper.map(listPesanan, new TypeToken<List<PesananDTO>>() {}.getType());
            mapResult = transformToDTO.transformObject(objectMapper,listPesananDTO,pagePesanan,mapColumnSearch);

        }
        catch (Exception e)
        {
            strExceptionArr[1] = "findAllPesanan(Pageable pageable, WebRequest request) --- LINE 157";
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

    // METHOD FIND BY PAGE BERFUNGSI UNTUK MENGAMBIL DATA PESANAN BY PAGE
    public Map<String,Object> findByPage(Pageable pageable,WebRequest request,String columFirst,String valueFirst)
    {
        Page<Pesanan> pagePesanan = null;
        List<Pesanan> listPesanan = null;
        List<PesananDTO> listPesananDTO = null;
        Map<String,Object> mapResult = null;

        try
        {
            if(columFirst.equals("id") || columFirst.equals("harga") || columFirst.equals("berat") || columFirst.equals("total"))
            {
                if(!valueFirst.equals("") && valueFirst!=null)
                {
                    try
                    {
                        if (columFirst.equals("id")){
                            Long.parseLong(valueFirst);
                        }
                        else {
                            Double.parseDouble(valueFirst);
                        }
                    }
                    catch (Exception e)
                    {
                        strExceptionArr[1] = "findByPage(Pageable pageable,WebRequest request,String columFirst,String valueFirst) --- LINE 215";
                        LoggingFile.exceptionStringz(strExceptionArr, e, OtherConfig.getFlagLogging());
                        return new ResponseHandler().
                                generateModelAttribut(ConstantMessage.WARNING_DATA_EMPTY,
                                        HttpStatus.OK,
                                        transformToDTO.transformObjectDataEmpty(objectMapper,pageable,mapColumnSearch),//HANDLE NILAI PENCARIAN
                                        "FE11005",
                                        request);
                    }
                }
            }
            pagePesanan = getDataByValue(pageable,columFirst,valueFirst);
            listPesanan = pagePesanan.getContent();
            if(listPesanan.size()==0)
            {
                return new ResponseHandler().
                        generateModelAttribut(ConstantMessage.WARNING_DATA_EMPTY,
                                HttpStatus.OK,
                                transformToDTO.transformObjectDataEmpty(objectMapper,pageable,mapColumnSearch),//HANDLE NILAI PENCARIAN EMPTY
                                "FV03006",
                                request);
            }
            listPesananDTO = modelMapper.map(listPesanan, new TypeToken<List<PesananDTO>>() {}.getType());
            mapResult = transformToDTO.transformObject(objectMapper,listPesananDTO,pagePesanan,mapColumnSearch);
            System.out.println("LIST DATA => "+listPesananDTO.size());
        }

        catch (Exception e)
        {
            strExceptionArr[1] = "findByPage(Pageable pageable,WebRequest request,String columFirst,String valueFirst) --- LINE 199";
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

    // METHOD FIND BY ID BERFUNGSI UNTUK MENGAMBIL DATA PESANAN DENGAN ID
    public Map<String,Object> findById(Long id, WebRequest request)
    {
        Pesanan pesanan = pesananRepo.findById(id).orElseThrow (
                ()-> null
        );
        if(pesanan == null)
        {
            return new ResponseHandler().generateModelAttribut(ConstantMessage.WARNING_PESANAN_NOT_EXISTS,
                    HttpStatus.NOT_ACCEPTABLE,
                    transformToDTO.transformObjectDataEmpty(objectMapper,mapColumnSearch),
                    "FV03005",request);
        }
        PesananDTO pesananDTO = modelMapper.map(pesanan, new TypeToken<PesananDTO>() {}.getType());
        return new ResponseHandler().
                generateModelAttribut(ConstantMessage.SUCCESS_FIND_BY,
                        HttpStatus.OK,
                        pesananDTO,
                        null,
                        request);
    }

    public List<PesananDTO> getAllPesanan()//KHUSUS UNTUK FORM INPUT SAJA
    {
        List<PesananDTO> listPesananDTO = null;
        Map<String,Object> mapResult = null;
        List<Pesanan> listPesanan = null;

        try
        {
            listPesanan = pesananRepo.findByIsDelete((byte)1);
            if(listPesanan.size()==0)
            {
                return new ArrayList<PesananDTO>();
            }
            listPesananDTO = modelMapper.map(listPesanan, new TypeToken<List<PesananDTO>>() {}.getType());
        }
        catch (Exception e)
        {
            strExceptionArr[1] = "getAllPesanan() --- LINE 304";
            LoggingFile.exceptionStringz(strExceptionArr, e, OtherConfig.getFlagLogging());
            return listPesananDTO;
        }
        return listPesananDTO;
    }

    // METHOD DELETE PESANAN BERFUNGSI UNTUK MENGHAPUS DATA PESANAN
    public Map<String, Object> deletePesanan(Long idPesanan, WebRequest request) {
        String strMessage = ConstantMessage.SUCCESS_DELETE;
        Object strUserIdz = request.getAttribute("USR_ID",1);
        Pesanan nextPesanan = null;
        try {
            nextPesanan = pesananRepo.findById(idPesanan).orElseThrow(
                    ()->null
            );

            if(nextPesanan==null)
            {
                return new ResponseHandler().generateModelAttribut(ConstantMessage.WARNING_DEMO_NOT_EXISTS,
                        HttpStatus.NOT_ACCEPTABLE,
                        transformToDTO.transformObjectDataEmpty(objectMapper,mapColumnSearch),
                        "FV11008",request);
            }
            if(strUserIdz==null)
            {
                return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_FLOW_INVALID,
                        HttpStatus.NOT_ACCEPTABLE,
                        null,
                        "FV11009",request);
            }
            nextPesanan.setIsDelete((byte)0);
            nextPesanan.setModifiedBy(Integer.parseInt(strUserIdz.toString()));
            nextPesanan.setModifiedDate(new Date());

        } catch (Exception e) {
            strExceptionArr[1] = " deletePesanan(Long idPesanan, WebRequest request)  --- LINE 304";
            LoggingFile.exceptionStringz(strExceptionArr, e, OtherConfig.getFlagLogging());
            return new ResponseHandler().generateModelAttribut(ConstantMessage.ERROR_SAVE_FAILED,
                    HttpStatus.BAD_REQUEST,
                    transformToDTO.transformObjectDataEmpty(objectMapper,mapColumnSearch),
                    "FE11010", request);
        }
        return new ResponseHandler().generateModelAttribut(strMessage,
                HttpStatus.OK,
                transformToDTO.transformObjectDataEmpty(objectMapper,mapColumnSearch),
                null, request);
    }

    // METHOD GET DATA BY VALUE BERFUNGSI MENGAMBIL DATA DENGAN VALUE
    private Page<Pesanan> getDataByValue(Pageable pageable, String paramColumn, String paramValue)
    {
        if(paramValue.equals(""))
        {
            return pesananRepo.findByIsDelete(pageable,(byte) 1);
        }
        if(paramColumn.equals("id")){
            return pesananRepo.findByIsDeleteAndIdPesanan(pageable,(byte) 1,Long.parseLong(paramValue));
        } else if(paramColumn.equals("nama")){
            return pesananRepo.findByIsDeleteAndPelangganNamaLengkapContainsIgnoreCase(pageable,(byte) 1,paramValue);
        } else if(paramColumn.equals("harga")){
            return pesananRepo.findByIsDeleteAndPaketLayananHargaPerKilo(pageable,(byte) 1,Double.parseDouble(paramValue));
        } else if(paramColumn.equals("total")){
            return pesananRepo.findByIsDeleteAndTotalHarga(pageable,(byte) 1,Double.parseDouble(paramValue));
        } else if(paramColumn.equals("berat")){
            return pesananRepo.findByIsDeleteAndBerat(pageable,(byte) 1,Double.parseDouble(paramValue));
        } else if(paramColumn.equals("layanan")){
            return pesananRepo.findByIsDeleteAndPaketLayananNamaPaketContainsIgnoreCase(pageable,(byte) 1,paramValue);
        } else if(paramColumn.equals("tipe")){
            return pesananRepo.findByIsDeleteAndPaketLayananTipeLayananContainsIgnoreCase(pageable,(byte) 1,paramValue);
        } else if(paramColumn.equals("cara")){
            return pesananRepo.findByIsDeleteAndPembayaranNamaPembayaranContainsIgnoreCase(pageable,(byte) 1,paramValue);
        }

        return pesananRepo.findByIsDelete(pageable,(byte) 1);
    }

    // METHOD GET DATA TO EXPORT BERFUNGSI UNTUK MEMBUAT LAPORAN PDF
    private List<Pesanan> getDataToExport(String paramColumn, String paramValue)
    {
        if(paramValue.equals(""))
        {
            return pesananRepo.findByIsDelete((byte) 1);
        }
        if(paramColumn.equals("id"))
        {
            return pesananRepo.findByIsDeleteAndIdPesanan((byte) 1,Long.parseLong(paramValue));
        } else if (paramColumn.equals("nama")) {
            return pesananRepo.findByIsDeleteAndPelangganNamaLengkapContainsIgnoreCase((byte) 1,paramValue);
        }

        return pesananRepo.findByIsDelete((byte) 1);
    }
    public List<PesananDTO> dataToExport(WebRequest request,String columnFirst,String valueFirst)
    {
        List<Pesanan> listPesanan = null;
        List<PesananDTO> listPesananDTO = null;
        Map<String,Object> mapResult = null;

        try
        {
            if(columnFirst.equals("id"))
            {
                try
                {
                    Long.parseLong(valueFirst);
                }
                catch (Exception e)
                {
                    strExceptionArr[1] = "dataToExport(WebRequest request,String columFirst,String valueFirst) --- LINE 209";
                    LoggingFile.exceptionStringz(strExceptionArr, e, OtherConfig.getFlagLogging());
                    return new ArrayList<PesananDTO>();
                }
            }
            listPesanan = getDataToExport(columnFirst,valueFirst);
            if(listPesanan.size()==0)
            {
                return new ArrayList<PesananDTO>();
            }
            listPesananDTO = modelMapper.map(listPesanan, new TypeToken<List<PesananDTO>>() {}.getType());
        }

        catch (Exception e)
        {
            strExceptionArr[1] = "dataToExport(WebRequest request,String columFirst,String valueFirst) --- LINE 243";
            LoggingFile.exceptionStringz(strExceptionArr, e, OtherConfig.getFlagLogging());
            return new ArrayList<PesananDTO>();
        }
        return listPesananDTO;
    }
}
