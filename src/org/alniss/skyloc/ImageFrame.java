package org.alniss.skyloc;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

class ImageFrame extends JFrame {

    public ImageFrame(){
        setTitle("ImageTest");
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        ImageComponent component = new ImageComponent();
        add(component);

    }

    public static final int DEFAULT_WIDTH = 800;
    public static final int DEFAULT_HEIGHT = 700;
}


class ImageComponent extends JComponent{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Image image;
    public ImageComponent(){
//        try{
            File image2 = new File("bishnu.jpg");
//            image = ImageIO.read(image2);
            image = ImagePanel.toImage(InversePerspectiveTransform.inversePerspectiveTransform());

//        }
//        catch (IOException e){
//            e.printStackTrace();
//        }
    }
    public void paintComponent (Graphics g){
        if(image == null) return;
        int imageWidth = image.getWidth(this);
        int imageHeight = image.getHeight(this);

        g.drawImage(image, 10, 10, this);

        for (int i = 0; i*imageWidth <= getWidth(); i++)
            for(int j = 0; j*imageHeight <= getHeight();j++)
                if(i+j>0) g.copyArea(0, 0, imageWidth, imageHeight, i*imageWidth, j*imageHeight);
    }

}
