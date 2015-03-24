//------------------------------------------------------------------------------------------------//
//                                                                                                //
//                                   B o o k P d f O u t p u t                                    //
//                                                                                                //
//------------------------------------------------------------------------------------------------//
// <editor-fold defaultstate="collapsed" desc="hdr">
//  Copyright © Hervé Bitteur and others 2000-2014. All rights reserved.
//  This software is released under the GNU General Public License.
//  Goto http://kenai.com/projects/audiveris to report bugs or suggestions.
//------------------------------------------------------------------------------------------------//
// </editor-fold>
package omr.score.ui;

import omr.sheet.Book;
import omr.sheet.Sheet;
import omr.sheet.ui.SheetResultPainter;

import com.itextpdf.text.Document;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Class {@code BookPdfOutput} produces a physical PDF output of a book.
 *
 * @author Hervé Bitteur
 */
public class BookPdfOutput
{
    //~ Static fields/initializers -----------------------------------------------------------------

    private static final Logger logger = LoggerFactory.getLogger(BookPdfOutput.class);

    //~ Instance fields ----------------------------------------------------------------------------
    /** The related book. */
    private final Book book;

    /** The file to print to. */
    private final File file;

    //~ Constructors -------------------------------------------------------------------------------
    /**
     * Creates a new SheetPdfOutput object.
     *
     * @param book the book to print
     * @param file the target PDF file
     */
    public BookPdfOutput (Book book,
                          File file)
    {
        this.book = book;
        this.file = file;
    }

    //~ Methods ------------------------------------------------------------------------------------
    /**
     * Write the PDF output for the provided sheet if any, otherwise for the whole book.
     *
     * @param sheet desired sheet or null
     * @throws Exception if printing goes wrong
     */
    public void write (Sheet sheet)
            throws Exception
    {
        FileOutputStream fos = null;
        Document document = null;
        PdfWriter writer = null;

        try {
            final List<Sheet> sheets = (sheet != null) ? Arrays.asList(sheet) : book.getSheets();
            fos = new FileOutputStream(file);

            for (Sheet sh : sheets) {
                Dimension dim = sh.getDimension();

                if (document == null) {
                    document = new Document(new Rectangle(dim.width, dim.height));
                    writer = PdfWriter.getInstance(document, fos);
                    document.open();
                } else {
                    document.setPageSize(new Rectangle(dim.width, dim.height));
                    document.newPage();
                }

                PdfContentByte cb = writer.getDirectContent();
                Graphics2D g2 = cb.createGraphics(dim.width, dim.height);

                // Scale: 1
                g2.scale(1, 1);

                // Anti-aliasing ON
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // Painting
                SheetResultPainter painter = new SheetResultPainter(
                        sh,
                        g2,
                        false, // No voice painting
                        true, // Paint staff lines
                        false); // No annotations
                g2.setColor(Color.BLACK);

                painter.process();

                // This is the end...
                g2.dispose();
            }
        } finally {
            if (document != null) {
                document.close();
            }

            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}