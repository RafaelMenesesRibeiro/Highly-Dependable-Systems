package hds.security;

import pteidlib.PTEID_Certif;
import pteidlib.PteidException;
import pteidlib.pteid;
import sun.security.provider.X509Factory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class AuthCertExtractor {

    public static void main(String[] args)  {

        if (args.length != 1) {
            System.out.println("Pass argument to output file");
            return;
        }

        try {
            System.loadLibrary("pteidlibj");
            pteid.Init("");
            pteid.SetSODChecking(false);
            X509Certificate cert = getAuthCertificate();
            System.out.println("Citizen Authentication Certificate "+cert);
            certToPEMFile(cert, args[0]);
        } catch (Throwable e) {
            System.out.println("[Catch] Exception: " + e.getLocalizedMessage());
        }
    }

    private static X509Certificate getAuthCertificate() throws CertificateException {
        byte[] certificate_bytes = null;
        try {
            PTEID_Certif[] certs = pteid.GetCertificates();
            certificate_bytes = certs[0].certif;
            pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD);
        } catch (PteidException e) {
            e.printStackTrace();
        }
        return getCertFromByteArray(certificate_bytes);
    }
    
    private static X509Certificate getCertFromByteArray(byte[] certificateEncoded) throws CertificateException{
        CertificateFactory f = CertificateFactory.getInstance("X.509");
        InputStream in = new ByteArrayInputStream(certificateEncoded);
        return (X509Certificate)f.generateCertificate(in);
    }

    private static void certToPEMFile(X509Certificate cert, String outputFile) throws IOException, CertificateEncodingException {
        String content = X509Factory.BEGIN_CERT + '\n' + ConvertUtils.bytesToBase64String(cert.getEncoded())  + '\n' + X509Factory.END_CERT;
        Files.write(Paths.get(outputFile), content.getBytes());
    }
}
