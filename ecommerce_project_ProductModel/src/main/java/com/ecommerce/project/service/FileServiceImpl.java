package com.ecommerce.project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService{

    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {
        //file names of current /original file
        String originalFileName=file.getOriginalFilename();
        //generate a unique file(uuid)
        String randomId= UUID.randomUUID().toString();
        //mat.jpg--->1234--->1234.jpg
        assert originalFileName != null;
        String fileName=randomId.concat(originalFileName.substring(originalFileName.lastIndexOf('.')));
//        String filePath=path+ File.pathSeparator+fileName; //----->"/"

        String filePath = path + File.separator + fileName; // ✅ RIGHT

        //check if path exists or just create one
        File folder=new File(path);
        if(!folder.exists()){
            folder.mkdir();
        }
        //upload to server
        Files.copy(file.getInputStream(), Paths.get(filePath));
        //returning file name
        return fileName;


    }
}
