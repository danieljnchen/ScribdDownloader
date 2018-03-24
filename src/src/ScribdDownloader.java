import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Image;

public class ScribdDownloader {
    static Document document;
    public static void main(String[] args) throws Exception {
        System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");

        System.out.print("URL: ");
        String urlText = readLine();
        String docNum = urlText.substring(urlText.indexOf(".com/") + 5);
        docNum = docNum.substring(docNum.indexOf("/")+1);
        docNum = docNum.substring(0,docNum.indexOf("/"));

        File ScribdDownloads = new File(System.getProperty("user.dir") + "/ScribdDownloads");
        if(!ScribdDownloads.exists()) {
            ScribdDownloads.mkdir();
        }

        System.out.print("PDF name: ");
        String fileName = readLine();
        File pdf = new File(System.getProperty("user.dir") + "/ScribdDownloads", fileName + ".pdf");
        document = new Document();

        PdfWriter.getInstance(document, new FileOutputStream(pdf));
        document.open();

        String sourceURL = "https://scribd.com/embeds/" + docNum + "/content";
        String webpage = downloadWebpage(new URL(sourceURL));
        BufferedReader br = new BufferedReader(new StringReader(webpage));

        String line;
        int page = 1;

        try {
            while ((line = br.readLine()) != null) {
                String imageURLtext = null;

                if (line.contains("http://html.scribd.com")) {
                    imageURLtext = line.substring(line.indexOf("http://"), line.indexOf(".jpg") + 4);
                    System.out.println(imageURLtext);
                }
                if (line.contains("contentUrl")) {
                    line = line.substring(line.indexOf("\"") + 1);
                    line = line.substring(0, line.indexOf("\""));
                    URL url = new URL(line);
                    String subpage = downloadWebpage(url);
                    imageURLtext = subpage.substring(subpage.indexOf("http://"), subpage.indexOf(".jpg") + 4);
                    System.out.println(imageURLtext);
                }

                if (imageURLtext != null) {
                    downloadImage(new URL(imageURLtext), String.valueOf(page) + ".jpg");
                    ++page;
                }
            }

            System.out.println("PDF generated.");
        } catch(Exception e) {
            System.out.println("Unfortunately, this link is not compatible with this program. Your download was stopped on page " + (page-1) + ".");
        } finally {
            document.close();
        }
    }

    public static void downloadImage(URL url, String name) throws Exception {
        Image im = Image.getInstance(url);
        im.setAbsolutePosition(0,0);
        im.setBorderWidth(0);
        im.scaleAbsoluteHeight(PageSize.A4.getHeight());
        im.scaleAbsoluteWidth(PageSize.A4.getWidth());
        document.newPage();
        document.add(im);
    }

    public static String downloadWebpage(URL url) throws IOException {
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestProperty("Accept-Encoding", "gzip");
        InputStream is = new GZIPInputStream(con.getInputStream());
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line;
        String out = "";

        while ((line = br.readLine()) != null) {
            out = out.concat("\n" + line);
        }
        return out;
    }

    public static String readLine() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        return br.readLine();
    }
}