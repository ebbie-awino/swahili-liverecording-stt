/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package liverecording;

/**
 *
 * @author Ebbie
 */
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JOptionPane;

import java.io.FileInputStream;
import java.io.InputStream;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;


public class LiveRecording {

    /**
     * @param args the command line arguments
     */
   public static String transcript;
    
    private File file;
    private String soundFileName;
    private String filename = "samples_";
    private int suffix = 0;
    private AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
    private int MONO = 1;
    private AudioFormat format = new AudioFormat(
              AudioFormat.Encoding.PCM_SIGNED,16000, 16, MONO, 2, 16000, true);
    private TargetDataLine Record;
    
    File getNewFile() {
       try {
         do {
            soundFileName = filename + (suffix++) + "."+ fileType.getExtension();
            file = new File(soundFileName);
         } while (!file.createNewFile());
       } catch (IOException ex) {
         ex.printStackTrace();
       }
       return file;
    }
    public void startRecording() throws Exception {
        try {
            new Thread() {
                public void run() {
                    DataLine.Info info = new DataLine.Info(TargetDataLine.class,format);
                    if (!AudioSystem.isLineSupported(info)) {
                        JOptionPane.showMessageDialog(null, "Line not supported"
                                + info, "Line not supported",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    try {
                        Record = (TargetDataLine) AudioSystem.getLine(info);
                        Record.open(format, Record.getBufferSize());
                        AudioInputStream sound = new AudioInputStream(Record);
                        System.out.println("Starting recording...");
                        Record.start();
                        AudioSystem.write(sound, fileType, file);
                    } catch (LineUnavailableException ex) {
                        JOptionPane.showMessageDialog(null, "Line not available"
                                + ex, "Line not available",
                                JOptionPane.ERROR_MESSAGE);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "I/O Error " + ex,
                                "I/O Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.start();
            Thread.sleep(10000);
            Record.stop();
            Record.close();
            System.out.println("Recording has ended.");
            
            String directoryFilePath = "C:\\Users\\Ebbie\\Documents\\NetBeansProjects\\LiveRecording";
            System.out.println(getLastModified(directoryFilePath));

            Configuration configuration = new Configuration();

            configuration.setAcousticModelPath("src//acoustic");
            configuration.setDictionaryPath("src//language_model//dictionary.dic");
            configuration.setLanguageModelPath("src//language_model//language.lm");

            StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(configuration);
            InputStream stream = new FileInputStream(getLastModified(directoryFilePath));

            recognizer.startRecognition(stream);
            SpeechResult result;

            while ((result = recognizer.getResult()) != null) {
                transcript= result.getHypothesis();
                System.out.format("Hypothesis: %s\n", result.getHypothesis());
            }

            recognizer.stopRecognition();
        } catch (InterruptedException ex) {
            Logger.getLogger(LiveRecording.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void stopRecording() {
       Record.stop();
       Record.close();
    }
    
    public static File getLastModified(String directoryFilePath)
    {
        File directory = new File(directoryFilePath);
        File[] files = directory.listFiles(File::isFile);
        long lastModifiedTime = Long.MIN_VALUE;
        File chosenFile = null;

        if (files != null)
        {
            for (File file : files)
            {
                if (file.lastModified() > lastModifiedTime)
                {
                    chosenFile = file;
                    lastModifiedTime = file.lastModified();
                }
            }
        }

        return chosenFile;
    }
 
    public static void main(String[] args) throws Exception {
        LiveRecording rec = new LiveRecording();
        File f = rec.getNewFile();
        rec.startRecording();
        
        
    }
}
