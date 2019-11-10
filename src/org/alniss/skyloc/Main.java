package org.alniss.skyloc;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        JFrame f=new JFrame();//creating instance of JFrame

//        JButton b=new JButton("click");//creating instance of JButton
//        b.setBounds(130,100,100, 40);//x axis, y axis, width, height
//
//        f.add(b);//adding button in JFrame
//        ImagePanel imagePanel = new ImagePanel(InversePerspectiveTransform.inversePerspectiveTransform());
//        ImagePanel imagePanel = new ImagePanel(InversePerspectiveTransform.loadMat(new File(System.getProperty("user.dir") + "/data/book2.jpg")));
//        f.add(imagePanel);
        InversePerspectiveTransform.inversePerspectiveTransform();

        f.setSize(400,500);//400 width and 500 height
        f.setLayout(null);//using no layout managers
        f.setVisible(true);//making the frame visible
    }
}
