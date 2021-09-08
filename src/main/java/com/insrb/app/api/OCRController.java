package com.insrb.app.api;

import com.insrb.app.mapper.IN006TMapper;
import com.insrb.app.util.ResourceUtil;
import com.insrb.app.util.StorageService;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ocr")
public class OCRController {

    private static final String TMAX_KEY = "key";

    @Value("classpath:basic/test_datafromtmax.json")
    private Resource test_datafromtamx_json;

    private JSONObject jsonInsurobo;

    @Autowired
    StorageService storageService;

    @Autowired
    IN006TMapper in006TMapper;


    @PostMapping(path = "/businesslicense")
    @ResponseBody
    public Map<String, Object> sendBusinessLicenseToTmax(
            @RequestParam("file") MultipartFile file
    ) {
        Map<String, Object> field_data = null;
        try{
            String filename = file.getOriginalFilename();

             this.jsonInsurobo = ResourceUtil.asJSONObject(test_datafromtamx_json);
             Map<String, Object> data = jsonInsurobo.toMap();

            File TmaxFile = convertFile(file);

            //Tmax API Request.
            HttpResponse<String> response = Unirest.post("http://220.90.208.159:8282/upload")
                    .field("file", TmaxFile)
                    .asString();

            log.debug(response.getBody());

            data.put("TMAX", response.getBody());

            return data;
        } catch (Exception e) {
            log.debug(e.getMessage());
        }
        return field_data;
    }

    @PostMapping(path = "/dirstore")
    @ResponseBody
    public Boolean saveImageToInsuroboDir( //Directory Store
            @RequestParam("file") MultipartFile file
    ) {
        try{
            boolean check;
            String filename = file.getOriginalFilename();

            storageService.store(file); //파일 저장
            Resource resource = storageService.loadAsResource(filename); //리소스 체크

            if(resource.exists())
            {
                check = true;
            } else{
                check = false;
            }

            return check;
        } catch (Exception e) {
            log.debug(e.getMessage());
        }
        return false;
    }

    public File convertFile(MultipartFile mfile) throws IOException {
        File file = new File(mfile.getOriginalFilename());
        file.createNewFile();

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(mfile.getBytes());
        fos.close();

        return file;
    }
}
