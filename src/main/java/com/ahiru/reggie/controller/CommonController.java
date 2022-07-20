package com.ahiru.reggie.controller;

import com.ahiru.reggie.common.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
public class CommonController {

    //从配置文件中取出目标目录path
    @Value("${reggie.path}")
    private String basePath;

    //接收上传文件
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){  //参数名必须与上传的文件名字一致
        //接收file之后会作为临时文件存放在临时目录，请求结束后自动删除

        //获取原始文件名
        String originalFilename = file.getOriginalFilename();

        //原始文件名可能重复，所以一般是使用UUID随机生成的文件名，并拼接后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));//获取后缀
        String newFileName = UUID.randomUUID().toString() + suffix;

        //如果目标目录不存在，则创建一个
        File dir = new File(basePath);
        if(!dir.exists()){
            dir.mkdirs();
        }

        //转存文件
        try {
            file.transferTo(new File(basePath + newFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(newFileName);
    }



    //给客户端发送文件
    @GetMapping("/download")
    public void download(String name, HttpServletResponse httpServletResponse){ //name是前端发送的文件名 ?name=xx
        try {
            //输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath+name));

            //创建输出流对象
            ServletOutputStream outputStream = httpServletResponse.getOutputStream(); //从response获取

            //指定输出文件类型
            httpServletResponse.setContentType("image/jpeg");

            //开始输出-将图片传回浏览器
            int len = 0;
            byte[] bytes = new byte[1024];
            while((len = fileInputStream.read(bytes)) != -1){
                outputStream.write(bytes, 0, len); //写入数据，长度为从0到len
                outputStream.flush();
            }
            outputStream.close();
            fileInputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }



    }

}
