package client;


import javafx.scene.image.Image;
import javafx.scene.paint.Color;


import java.io.Serializable;

public class Packet implements Serializable {
    private String message;
    private String userName;
    private String image;
    private String fontColor;
    private String fontStyle;

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public String getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(String fontStyle) {
        this.fontStyle = fontStyle;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Packet(String message, String userName, String image ,  String fontColor, String fontStyle) {
        this.message = message;
        this.userName = userName;
        this.image = image;
        this.fontColor =fontColor;
        this.fontStyle = fontStyle;
    }
    public String toString(){
        return userName + " : " + this.message ;
    }
}

 class MyColor implements Serializable{
    private Color color;

     public MyColor(Color color) {
         this.color = color;
     }

     public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}

