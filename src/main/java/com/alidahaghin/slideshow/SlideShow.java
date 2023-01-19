package com.alidahaghin.slideshow;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SlideShow extends JFrame implements KeyListener {

    public static void main(String[] args) {
        SlideShow slideShow = new SlideShow();
        slideShow.init();
    }

    private static final Logger logger = Logger.getLogger(SlideShow.class.getName());
    private static final Color BACKGROUND_COLOR = new Color(0x333333);
    private static final int MINIMUM_SLIDE_SHOW_INTERVAL = 500;
    private static final int STOP_DELAY_MILLISECOND = 100;

    private static final int KEY_CODE_ESCAPE = 27;
    private static final int KEY_CODE_SPACE = 32;
    private static final int KEY_CODE_LEFT_ARROW = 37;
    private static final int KEY_CODE_UP_ARROW = 38;
    private static final int KEY_CODE_RIGHT_ARROW = 39;
    private static final int KEY_CODE_DOWN_ARROW = 40;

    private List<File> picFiles = new ArrayList<>();
    private JLabel previousPic;
    private int currentIndex = 0;
    private JButton chooseDirButton;
    private File chosenDir;
    private boolean weAreInSlideShow = false;
    private boolean stopped = false;
    private Thread changePicThread;
    private int slideShowInterval = 2000;

    public void init() {
        this.setTitle("SlideShow");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setResizable(true);
        this.setSize(500, 500);
        this.setLayout(new GridBagLayout());
        this.addKeyListener(this);
        this.setVisible(true);
        this.getContentPane().setBackground(BACKGROUND_COLOR);

        addChooseDirButton();
    }

    private void addChooseDirButton() {
        this.chooseDirButton = new JButton();
        this.chooseDirButton.setText("Choose Folder");
        this.chooseDirButton.setFocusable(false);
        this.chooseDirButton.setBounds(100, 215, 300, 40);
        this.chooseDirButton.addActionListener(this::chooseFile);
        this.add(chooseDirButton, new GridBagConstraints());
    }

    private void chooseFile(ActionEvent actionEvent) {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setCurrentDirectory(new File("."));
        jFileChooser.setAcceptAllFileFilterUsed(false);
        jFileChooser.setFileHidingEnabled(false);
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int response = jFileChooser.showOpenDialog(null);

        if (response == JFileChooser.APPROVE_OPTION) {
            startSlideShow(jFileChooser);
        }
    }

    private void startSlideShow(JFileChooser jFileChooser) {
        chosenDir = new File(jFileChooser.getSelectedFile().getAbsolutePath());
        chooseDirButton.setVisible(false);

        weAreInSlideShow = true;

        loadPics();
        startChangePicThread();
    }

    private void loadPics() {
        try {
            picFiles = Files.walk(Paths.get(chosenDir.getPath()))
                    .filter(s -> s.getFileName().toString().endsWith(".jpg"))
                    .map(Path::toFile).collect(Collectors.toList());

            showPicture(picFiles.get(0));
        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "shit happened in loadPics");
        }
    }

    private void startChangePicThread() {
        changePicThread = new Thread(this::startChangePicLoop);
        changePicThread.start();
    }

    private void startChangePicLoop() {
        while (true) {
            try {
                if (stopped) {
                    Thread.sleep(STOP_DELAY_MILLISECOND);
                    continue;
                }
                actualChangePic(ChangePicDirection.FORWARD);
                Thread.sleep(slideShowInterval);
            } catch (InterruptedException interruptedException) {
                break;
            } catch (Exception e) {
                e.printStackTrace();
                logger.log(Level.SEVERE, "it was close to end the change pic while loop");
            }
        }
    }

    private synchronized void actualChangePic(ChangePicDirection changePicDirection) throws IOException {
        File file = getNextFile(changePicDirection);

        if (previousPic != null) {
            this.remove(previousPic);
        }

        showPicture(file);

        repaint();
    }

    private File getNextFile(ChangePicDirection changePicDirection) {
        goToNextFileIndex(changePicDirection);
        return picFiles.get(currentIndex);
    }

    private void goToNextFileIndex(ChangePicDirection changePicDirection) {
        if (changePicDirection.equals(ChangePicDirection.FORWARD)) {
            currentIndex = (currentIndex + 1) % picFiles.size();
        } else {
            int newCounter = currentIndex - 1;
            if (newCounter < 0) {
                currentIndex = picFiles.size() + newCounter;
            } else {
                currentIndex = newCounter % picFiles.size();
            }
        }
    }

    private void showPicture(File file) throws IOException {
        BufferedImage bufferedImage = toBuff(file);
        JLabel jLabel = toJLabel(bufferedImage);
        previousPic = jLabel;
        this.add(jLabel);
    }

    private JLabel toJLabel(BufferedImage bufferedImage) {
        ImageIcon imageIcon = new ImageIcon(bufferedImage);
        Image scaledImage = scaleImage(bufferedImage, imageIcon);
        imageIcon = new ImageIcon(scaledImage);
        JLabel jLabel = new JLabel(imageIcon);
        jLabel.setBounds(0, 0, getWidth(), getHeight());

        return jLabel;
    }

    private Image scaleImage(BufferedImage bufferedImage, ImageIcon imageIcon) {
        return imageIcon.getImage().getScaledInstance(BigDecimal.valueOf(((double) bufferedImage.getWidth()) * (((double) getHeight()) / ((double) bufferedImage.getHeight()))).intValue(), getHeight(), Image.SCALE_SMOOTH);
    }

    private BufferedImage toBuff(File pic) throws IOException {
        try {
            return ImageIO.read(pic);

        } catch (IOException ioException) {
            logger.log(Level.WARNING, "shit happened with {}", pic.getPath());
            throw ioException;
        }
    }

    public void keyPressed(KeyEvent e) {
        if (weAreInSlideShow) {
            if (e.getKeyCode() == KEY_CODE_SPACE) {
                stopped = !stopped;
            }

            if (e.getKeyCode() == KEY_CODE_ESCAPE) {
                close();

            }

            if (e.getKeyCode() == KEY_CODE_LEFT_ARROW) {
                tryChangePic(ChangePicDirection.BACKWARD);
            }

            if (e.getKeyCode() == KEY_CODE_RIGHT_ARROW) {
                tryChangePic(ChangePicDirection.FORWARD);
            }

            if (e.getKeyCode() == KEY_CODE_UP_ARROW) {
                increaseSlideShowSpeedBy10Percent();
            }

            if (e.getKeyCode() == KEY_CODE_DOWN_ARROW) {
                decreaseSlideShowSpeedBy10Percent();
            }
        }
    }

    private void increaseSlideShowSpeedBy10Percent() {
        if (slideShowInterval > MINIMUM_SLIDE_SHOW_INTERVAL) {
            slideShowInterval = (slideShowInterval *9)/10;
        }
    }

    private void decreaseSlideShowSpeedBy10Percent() {
        slideShowInterval = (slideShowInterval * 11) / 10;
    }

    private void close() {
        previousPic.setVisible(false);
        changePicThread.interrupt();
        changePicThread = null;

        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    private void tryChangePic(ChangePicDirection direction) {
        try {
            actualChangePic(direction);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

}
