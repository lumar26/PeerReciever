//RECIEVER

package sample;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Main extends Application {

    private static int port;

    @Override
    public void init() throws Exception {
        super.init();
        this.port = 7000;
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Peer reciever");
        BorderPane root = new BorderPane();

        File audioFile = new File("/home/lumar26/Downloads/test.wav");
        Media audio = new Media(audioFile.toURI().toString());
        MediaPlayer audioPlayer = new MediaPlayer(audio);
        MediaView view = new MediaView(audioPlayer);

        Button btnPlay = new Button("Play");
        btnPlay.setOnAction(e -> {
            try {
                playAudio();
            } catch (SocketException socketException) {
                socketException.printStackTrace();
            } catch (Exception exception) {
                System.out.println("Došlo je do greške pri reprodukovanju audia");
                exception.printStackTrace();
            }
        });

        Button btnStop = new Button("Stop reproduction");
        btnStop.setOnAction(e -> {
            stopAudio();
        });
        root.setCenter(btnPlay);
        root.setBottom(btnStop);


        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }

    private void stopAudio() {

    }

    private void playAudio() throws IOException, LineUnavailableException, UnsupportedAudioFileException, SocketException {
        DatagramSocket socket = new DatagramSocket(port);

//        -----------------------------------------------------------------------------------------------------------------------------------------------
        //ovo je privremeno samo kako bi se znao format audia, to cemo verovatno morati da saljemo isto preko kontroolne veze kasnije
        AudioFormat audioFormat = AudioSystem.getAudioInputStream(new File("/home/lumar26/Downloads/dostojevski-bedni_ljudi.wav")).getFormat();
        SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, audioFormat));
        sourceLine.open(audioFormat);
        sourceLine.start();
//        -----------------------------------------------------------------------------------------------------------------------------------------------

        int framesize = AudioSystem.getAudioInputStream(new File("/home/lumar26/Downloads/dostojevski-bedni_ljudi.wav")).getFormat().getFrameSize();
        System.out.println("Veličina jednog okvira u bajtovima -----> " + framesize);
        byte[] recieveBuffer = new byte[1024 * framesize]; //i ovde bi trebalo da nam se posalje koja je velicina
        byte[] confirmationBuffer = "OK".getBytes();

        while (true) {//ovde bi bilo dobro da posiljalac posalje prvo kolko je veliki fajl, da bismo znali dokle vrtimo petlju, ili tako nesto
//            System.out.println("Krecemo da primamo pakete sa mreze");
            DatagramPacket recievePacket = new DatagramPacket(recieveBuffer, recieveBuffer.length);
            try {
                socket.receive(recievePacket);
            } catch (IOException e) {
                System.err.println("Problem na mreži, paket nije moguće primiti");
                e.printStackTrace();
            }
            System.out.println("Paket je primljen");

            sourceLine.write(recieveBuffer, 0, recieveBuffer.length);
            DatagramPacket confirmationPacket = new DatagramPacket(
                    confirmationBuffer, confirmationBuffer.length, recievePacket.getAddress(), recievePacket.getPort());
            socket.send(confirmationPacket); //paket potvrde omogućava da pošiljalac ne šalje pakete odmah, već da sačeka da se ceo bafer isprazni i ode ka mikseru
        }
//        System.out.println("-------------------------------Prekid rada-------------------------------");
    }


    public static void main(String[] args) {
        launch(args);
    }
}
