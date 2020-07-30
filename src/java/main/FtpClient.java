/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

/**
 *
 * @author sergy_000
 */
public class FtpClient {
 
    private String server = "31.24.152.74";
    private int port = 21;
    private String user = "jelastic-ftp";
    private String password = "password";
    private FTPClient ftp;
 
    // constructor
    public FtpClient() {
    }
 
    public void open() {
        try {
            ftp = new FTPClient();
            
            ftp.connect(server, port);
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                throw new IOException("Exception in connecting to FTP Server");
            }
            
            ftp.login(user, password);
            ftp.setFileType(FTPClient.IMAGE_FILE_TYPE);
        } catch (IOException ex) {
            
            Logger.getLogger(FtpClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
 
    void close() throws IOException {
        ftp.disconnect();
    }

    public FTPClient getFtp() {
        return ftp;
    }
    
}
