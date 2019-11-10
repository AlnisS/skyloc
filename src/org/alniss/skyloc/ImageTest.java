package org.alniss.skyloc;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;

public class ImageTest {
    public static void main(String[] args){
        EventQueue.invokeLater(new Runnable()
                               {
                                   public void run(){
                                       ImageFrame frame = new ImageFrame();
                                       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                                       frame.setVisible(true);


                                   }
                               }
        );
    }
}

