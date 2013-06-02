package CVImageProcessor.models;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Robert Wawrzyniak
 *         Date: 6/1/13
 *         Time: 6:39 PM
 */
public class PGM_Image {

    Logger logger = Logger.getLogger(PGM_Image.class);

    public int width;
    public int height;
    public int depth;
    public String comment = "";
    public String magicNumber;

    public String fileName;

    public byte[][] data;

    public File fileRef;

    public PGM_Image(File ref) {
        this.fileRef = ref;

        this.fileName = fileRef.getName();

        this.load();

    }


    private void load() {
        try {
            logger.debug("opening image");
            InputStream f = FileUtils.openInputStream(fileRef);
            BufferedReader d = new BufferedReader(new InputStreamReader(f));

            this.magicNumber = d.readLine();    // first line contains P2 or P5
            logger.debug("found magic number: " + magicNumber);

            String line = d.readLine();     // second line contains height and width
            while (line.startsWith("#")) {
                line = d.readLine();
                this.comment = line;
                logger.debug("found comment: \n" + comment);
            }

            Scanner s = new Scanner(line);

            this.width = s.nextInt();
            this.height = s.nextInt();
            logger.debug("found width/height: " + width + "/" + height);

            line = d.readLine();// third line contains maxVal
            s = new Scanner(line);
            this.depth = s.nextInt();
            byte[][] im = new byte[height][width];

            int count = 0;
            int b = 0;
            try {
                while (count < height*width) {
                    logger.debug("starting to read image data");
                    b = d.read() ;
                    if ( b < 0 )
                        break ;

                    if (b == '\n') { // do nothing if new line encountered
                    }
//                  else if (b == '#') {
//                      d.readLine();
//                  }
//                  else if (Character.isWhitespace(b)) { // do nothing if whitespace encountered
//                  }
                    else {
                        if ( "P5".equals(magicNumber) ) { // Binary format
                            /*im[count / width][count % width] = (byte)((b >> 8) & 0xFF);
                            count++;
                            im[count / width][count % width] = (byte)(b & 0xFF);
                            count++;*/

                            for(int row = 0; row < this.height; row++){
                                for(int col = 0; col < this.width; col++) im[row][col] = (byte) b;
                            }

                        }
                        else {  // ASCII format
                            im[count / width][count % width] = (byte)b ;
                            count++;
                        }
                    }
                }
                this.data = im;
                logger.debug("finishing reading image data");
            } catch (EOFException eof) {
                logger.error("EOFException thrown\n", eof);
            }
        }
        catch(Throwable t) {
            logger.error("", t);
            return;
        }

    }
}
