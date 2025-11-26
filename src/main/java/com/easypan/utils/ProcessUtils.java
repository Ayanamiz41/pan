package com.easypan.utils;

import com.easypan.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class ProcessUtils {
    private static final Logger logger = LoggerFactory.getLogger(ProcessUtils.class);

    public static String executeCommand(String cmd,Boolean outprintLog)throws BusinessException{
        if(StringTools.isEmpty(cmd)){
            logger.error("---要执行的FFmpeg指令为空，指令执行失败！---");
            return null;
        }
        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        try{
            process = Runtime.getRuntime().exec(cmd);
            PrintStream errorStream = new PrintStream(process.getErrorStream());
            PrintStream inputStream = new PrintStream(process.getInputStream());
            errorStream.start();
            inputStream.start();
            process.waitFor();
            String result = errorStream.stringBuffer.append(inputStream.stringBuffer + "\n").toString();
            if(outprintLog){
                logger.info("执行命令:{},已执行完毕，执行结果:[}",cmd,result);
            }else {
                logger.info("执行命令:{},已执行完毕",cmd);
            }
            return  result;
        } catch (Exception e) {
            logger.error("执行命令失败:{}",cmd,e);
            throw new BusinessException("视频转换失败");
        }finally {
            if(process != null){
                ProcessKiller ffmpegKiller = new ProcessKiller(process);
                runtime.addShutdownHook(ffmpegKiller);
            }
        }
    }

    private static class ProcessKiller extends Thread {
        private Process process;
        public ProcessKiller(Process process) {
            this.process = process;
        }
        public void run() {
            process.destroy();
        }
    }
    static class PrintStream extends Thread{
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        StringBuffer stringBuffer = new StringBuffer();
        public PrintStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }
        public void run() {
            try{
                if(inputStream==null){
                    return;
                }
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = null;
                while((line = bufferedReader.readLine())!=null){
                    stringBuffer.append(line);
                }
            } catch (Exception e) {
                logger.error("读取输入流出错了！错误信息:"+e.getMessage());
            }finally {
                try {
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }catch (IOException e){
                    logger.error("调用PrintStream读取输出流后，关闭流时出错！",e);
                }
            }
        }
    }
}
