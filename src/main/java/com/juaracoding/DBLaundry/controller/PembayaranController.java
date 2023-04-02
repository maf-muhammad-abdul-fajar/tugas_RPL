package com.juaracoding.DBLaundry.controller;/*
IntelliJ IDEA 2022.3.2 (Ultimate Edition)
Build #IU-223.8617.56, built on January 26, 2023
@Author User a.k.a. Safril Efendi Lubis
Java Developer
Created on 12/03/2023 14:33
@Last Modified 12/03/2023 14:33
Version 1.1
*/

import com.juaracoding.DBLaundry.configuration.OtherConfig;
import com.juaracoding.DBLaundry.dto.PembayaranDTO;
import com.juaracoding.DBLaundry.model.Pembayaran;
import com.juaracoding.DBLaundry.service.PembayaranService;
import com.juaracoding.DBLaundry.utils.ConstantMessage;
import com.juaracoding.DBLaundry.utils.ManipulationMap;
import com.juaracoding.DBLaundry.utils.MappingAttribute;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/mgmnt")
public class PembayaranController {
    private PembayaranService pembayaranService;
    @Autowired
    private ModelMapper modelMapper;
    private Map<String,Object> objectMapper = new HashMap<String,Object>();
    private Map<String,String> mapSorting = new HashMap<String,String>();
    private List<Pembayaran> lsCPUpload = new ArrayList<Pembayaran>();
    private String [] strExceptionArr = new String[2];
    private MappingAttribute mappingAttribute = new MappingAttribute();
    @Autowired
    public PembayaranController(PembayaranService pembayaranService) {
        strExceptionArr[0] = "PembayaranController";
        mapSorting();
        this.pembayaranService = pembayaranService;
    }
    private void mapSorting()
    {
        mapSorting.put("id","idPembayaran");
        mapSorting.put("nama","namaPembayaran");
    }

    //API GET BERFUNGSI UNTUK MEMUNCULKAN MODAL PEMBAYARAN
    @GetMapping("/v1/pembayaran/new")
    public String createPembayaran(Model model, WebRequest request)
    {
        //memastikan bahwa session user masih ada jika tidak ada maka akan di redirect ke logout
        if(OtherConfig.getFlagSessionValidation().equals("y"))
        {
            //memasukan model,objectmapper,request ke dalam mapping attribute
            //mendapatkan attribute user id dari service jika null maka akan redirect ke api logout
            mappingAttribute.setAttribute(model,objectMapper,request);//untuk set session
            if(request.getAttribute("USR_ID",1)==null){
                return "redirect:/api/check/logout";
            }
        }
        //memasukan attribute pembayaran ke create_pembayaran.html
        model.addAttribute("pembayaran", new PembayaranDTO());
        return "pembayaran/create_pembayaran";
    }

    //API GET BERFUNGSI UNTUK MEMUNCULKAN MODAL EDIT PEMBAYARAN
    @GetMapping("/v1/pembayaran/edit/{id}")
    public String editPembayaran(Model model, WebRequest request, @PathVariable("id") Long id)
    {
        //memastikan bahwa session user masih ada jika tidak ada maka akan di redirect ke logout
        if(OtherConfig.getFlagSessionValidation().equals("y"))
        {
            //memasukan model,objectmapper,request ke dalam mapping attribute
            //mendapatkan attribute user id dari service jika null maka akan redirect ke api logout
            mappingAttribute.setAttribute(model,objectMapper,request);//untuk set session
            if(request.getAttribute("USR_ID",1)==null){
                return "redirect:/api/check/logout";
            }
        }
        //objectMapper mengambil data pembayaran dengan findById(id,request)
        objectMapper = pembayaranService.findById(id,request);
        PembayaranDTO pembayaranDTO = (objectMapper.get("data")==null?null:(PembayaranDTO) objectMapper.get("data"));
        if((Boolean) objectMapper.get("success"))
        {
            //jika objectMapper bernilai TRUE atau success maka akan muncul modal edit_pembayaran.html
            //membuat object pembayaranDTOForSelect sebagai penampung dari paketlayananDTO data
            //fungsi pembayaranDTO adalah mendapatkan data dari variabel objectmapper jika null maka akan berisi null dan jika tidak maka akan terisi data dari pembayaran
            //kemudian add attribute pembayaranDTO ke dalam edit_paketlayanan.html
            PembayaranDTO pembayaranDTOForSelect = (PembayaranDTO) objectMapper.get("data");
            model.addAttribute("pembayaran", pembayaranDTO);
            return "pembayaran/edit_Pembayaran";
        }
        else
        {
            //jika tidak success atau FALSE maka akan redirect ke menu halaman awal pembayaran
            model.addAttribute("pembayaran", new PembayaranDTO());
            return "redirect:/api/mgmnt/v1/pembayaran/default";
        }
    }

    //API POST BERFUNGSI UNTUK MENAMBAHKAN DATA PEMBAYARAN BARU
    @PostMapping("/v1/pembayaran/new")
    public String newPembayaran(@ModelAttribute(value = "pembayaran")
                            @Valid PembayaranDTO pembayaranDTO
            , BindingResult bindingResult
            , Model model
            , WebRequest request
    )
    {
        //memastikan bahwa session user masih ada jika tidak ada maka akan di redirect ke logout
        if(OtherConfig.getFlagSessionValidation().equals("y"))
        {
            //memasukan model,objectmapper,request ke dalam mapping attribute
            //mendapatkan attribute user id dari service jika null maka akan redirect ke api logout
            mappingAttribute.setAttribute(model,objectMapper,request);//untuk set session
            if(request.getAttribute("USR_ID",1)==null){
                return "redirect:/api/check/logout";
            }
        }

        /* START VALIDATION */
        //jika data binding terjadi error maka akan muncul status error
        if(bindingResult.hasErrors())
        {
            model.addAttribute("pembayaran",pembayaranDTO);
            model.addAttribute("status","error");

            return "pembayaran/create_pembayaran";
        }
        //set isValid TRUE
        Boolean isValid = true;

        //jika isValid FALSE maka akan muncul modal create_pembayaran.html
        if(!isValid)
        {
            model.addAttribute("pembayaran",pembayaranDTO);
            return "pembayaran/create_pembayaran";
        }
        /* END OF VALIDATION */

        //mengisi objcet pembayaran dengan modelMapper dengan mapping pembayaranDTO berserta token
        //mengisi objectMapper dengan savePembayaran(pembayaran,request)
        Pembayaran pembayaran = modelMapper.map(pembayaranDTO, new TypeToken<Pembayaran>() {}.getType());
        objectMapper = pembayaranService.savePembayaran(pembayaran,request);
        if(objectMapper.get("message").toString().equals(ConstantMessage.ERROR_FLOW_INVALID))//AUTO LOGOUT JIKA ADA PESAN INI
        {
            //jika objectMapper terjadi error saat save data maka akan redirect logout
            return "redirect:/api/check/logout";
        }
        //jika objectMapper bernilai TRUE atau berhasil maka akan redirect ke menu awal pembayaran dan muncul message berhasil disimpan
        if((Boolean) objectMapper.get("success"))
        {
            //mapping attribute dengan parameter model dan object mapper
            //dan menampilkan message data berhasil disimpan
            //kemudian redirect ke menu awal pembayaran
            mappingAttribute.setAttribute(model,objectMapper);
            model.addAttribute("message","DATA BERHASIL DISIMPAN");
            Long idDataSave = objectMapper.get("idDataSave")==null?1:Long.parseLong(objectMapper.get("idDataSave").toString());
            return "redirect:/api/mgmnt/v1/pembayaran/fbpsb/0/asc/idPembayaran?columnFirst=id&valueFirst="+idDataSave+"&sizeComponent=5";//LANGSUNG DITAMPILKAN FOKUS KE HASIL EDIT USER TADI
        }
        //jika objectmapper bernilai false maka akan muncul pesan error
        else
        {
            //mapping attribute dengan error message binding result dan object mapper dan message
            //add atribute pembayaran dengan pembayaran dto
            //add atribute status error
            //return ke create_pembayaran.html
            mappingAttribute.setErrorMessage(bindingResult,objectMapper.get("message").toString());
            model.addAttribute("pembayaran",new PembayaranDTO());
            model.addAttribute("status","error");
            return "pembayaran/create_pembayaran";
        }
    }

    // API POST BERFUNGSI UNTUK MENGEDIT DATA PEMBAYARAN
    @PostMapping("/v1/pembayaran/edit/{id}")
    public String editPembayaran(@ModelAttribute("pembayaran")
                             @Valid PembayaranDTO pembayaranDTO
            , BindingResult bindingResult
            , Model model
            , WebRequest request
            , @PathVariable("id") Long id
    )
    {
        /* START VALIDATION */
        //jika data binding error maka akan return ke modal edit_pembayaran.html
        if(bindingResult.hasErrors())
        {
            model.addAttribute("pembayaran",pembayaranDTO);
            return "pembayaran/edit_pembayaran";
        }
        Boolean isValid = true;
        //jika isValid false maka akan return ke edit_pembayaran.html
        if(!isValid)
        {
            model.addAttribute("pembayaran",pembayaranDTO);
            return "pembayaran/edit_pembayaran";
        }
        /* END OF VALIDATION */

        //membuat object pembayaran dan mapping dengan pembayaranDTO beserta token
        //mengisi objectMapper dengan updatePembayaran(id,pembayaran,request)
        Pembayaran pembayaran = modelMapper.map(pembayaranDTO, new TypeToken<Pembayaran>() {}.getType());
        objectMapper = pembayaranService.updatePembayaran(id,pembayaran,request);
        if(objectMapper.get("message").toString().equals(ConstantMessage.ERROR_FLOW_INVALID))//AUTO LOGOUT JIKA ADA PESAN INI
        {
            //jika pesan object mapper bernilai error maka akan redirect logout
            return "redirect:/api/check/logout";
        }
            //jika object mapper bernilai true atau success maka akan redirect ke menu awal pembayaran
        if((Boolean) objectMapper.get("success"))
        {
            mappingAttribute.setAttribute(model,objectMapper);
            model.addAttribute("pembayaran",new PembayaranDTO());
            return "redirect:/api/mgmnt/v1/pembayaran/fbpsb/0/asc/idPembayaran?columnFirst=id&valueFirst="+id+"&sizeComponent=5";//LANGSUNG DITAMPILKAN FOKUS KE HASIL EDIT USER TADI
        }
        else
        {
            //mapping attrubute errir message dengan data message dari objectmapper
            //kemudian return ke modal edit_pembayaran.html
            mappingAttribute.setErrorMessage(bindingResult,objectMapper.get("message").toString());
            model.addAttribute("pembayaran",new PembayaranDTO());
            return "pembayaran/edit_pembayaran";
        }
    }

    //API GET BERFUNGSI UNTUK MENAMPILKAN HALAMAN MENU PEMBAYARAN
    @GetMapping("/v1/pembayaran/default")
    public String getDefaultData(Model model,WebRequest request)
    {

        //memastikan bahwa session user masih ada jika tidak ada maka akan di redirect ke logout
        if(OtherConfig.getFlagSessionValidation().equals("y"))
        {
            //memasukan model,objectmapper,request ke dalam mapping attribute
            //mendapatkan attribute user id dari service jika null maka akan redirect ke api logout
            mappingAttribute.setAttribute(model,objectMapper,request);//untuk set session
            if(request.getAttribute("USR_ID",1)==null){
                return "redirect:/api/check/logout";
            }
        }

        //membuat object pageable dan mendapatkan pagerequest dengan parameter page,size,dan sort by properties
        //mengisi objectmapper dengan findallpembayaran dengan parameter pageable dan request
        Pageable pageable = PageRequest.of(0,5, Sort.by("idPembayaran"));
        objectMapper = pembayaranService.findAllPembayaran(pageable,request);
        mappingAttribute.setAttribute(model,objectMapper,request);

        //masuk ke pembayaran.html
        model.addAttribute("pembayaran",new PembayaranDTO());
        model.addAttribute("sortBy","idPembayaran");
        model.addAttribute("currentPage",1);
        model.addAttribute("asc","asc");
        model.addAttribute("columnFirst","");
        model.addAttribute("valueFirst","");
        model.addAttribute("sizeComponent",5);
        return "pembayaran/pembayaran";
    }

    @GetMapping("/v1/pembayaran/fbpsb/{page}/{sort}/{sortby}")
    public String findByPembayaran(
            Model model,
            @PathVariable("page") Integer pagez,
            @PathVariable("sort") String sortz,
            @PathVariable("sortby") String sortzBy,
            @RequestParam String columnFirst,
            @RequestParam String valueFirst,
            @RequestParam String sizeComponent,
            WebRequest request
    ){
        //mengambil value dari request param
        //isi object pageable dengan pagerequest dengan parameter dari request param
        sortzBy = mapSorting.get(sortzBy);
        sortzBy = sortzBy==null?"idPembayaran":sortzBy;
        Pageable pageable = PageRequest.of(pagez==0?pagez:pagez-1,Integer.parseInt(sizeComponent.equals("")?"5":sizeComponent), sortz.equals("asc")?Sort.by(sortzBy):Sort.by(sortzBy).descending());
        objectMapper = pembayaranService.findByPage(pageable,request,columnFirst,valueFirst);
        mappingAttribute.setAttribute(model,objectMapper,request);
        model.addAttribute("pembayaran",new PembayaranDTO());
        model.addAttribute("currentPage",pagez==0?1:pagez);
        model.addAttribute("sortBy", ManipulationMap.getKeyFromValue(mapSorting,sortzBy));
        model.addAttribute("columnFirst",columnFirst);
        model.addAttribute("valueFirst",valueFirst);
        model.addAttribute("sizeComponent",sizeComponent);

        //masuk ke pembayaran.html
        return "pembayaran/pembayaran";
    }

    @GetMapping("/v1/pembayaran/delete/{id}")
    public String deletePembayaran(Model model
            , WebRequest request
            , @PathVariable("id") byte id
    ) {
        //memastikan bahwa session user masih ada jika tidak ada maka akan di redirect ke logout
        if (OtherConfig.getFlagSessionValidation().equals("y")) {
            //memasukan model,objectmapper,request ke dalam mapping attribute
            //mendapatkan attribute user id dari service jika null maka akan redirect ke api logout
            mappingAttribute.setAttribute(model, objectMapper, request);//untuk set session
            if (request.getAttribute("USR_ID", 1) == null) {
                return "redirect:/api/check/logout";
            }
        }

        //objectMapper mengambil data dengan method deletePembayaran(id,request)
        objectMapper = pembayaranService.deletePembayaran((long) id,request);
        if (objectMapper.get("message").toString().equals(ConstantMessage.ERROR_FLOW_INVALID))//AUTO LOGOUT JIKA ADA PESAN INI
        {
            //jika object mapper message adalah error maka redirect ke logout
            return "redirect:/api/check/logout";
        }

        //jika objectMapper bernilai TRUE atau success
        if ((Boolean) objectMapper.get("success")) {
            //mapping atribute berisi model dan objectMapper dan add attribute pembayaran,pembayaranDTO ke dalam pembayaran.html
            mappingAttribute.setAttribute(model, objectMapper);
            model.addAttribute("pembayaran", new PembayaranDTO());
            return "redirect:/api/mgmnt/v1/pembayaran/fbpsb/0/asc/idPembayaran?columnFirst=idPembayaran&valueFirst=" + id + "&sizeComponent=5";//LANGSUNG DITAMPILKAN FOKUS KE HASIL EDIT USER TADI
        } else {
            //jika objectMapper bernilai FALSE atau tidak success
            //mapping atribute berisi model dan objectMapper dan add attribute pembayaran,pembayaranDTO ke dalam pembayaran.html
            mappingAttribute.setAttribute(model, objectMapper);
            model.addAttribute("pembayaran", new PembayaranDTO());
            return "pembayaran/pembayaran";
        }
    }
}
