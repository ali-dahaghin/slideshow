
# Slideshow

Simple slideshow written in java using swing library.

I am not a swing guy and this is just for fun...

In this slideshow you can choose a folder to start showing all the .jpg files in there. (sure I could add .png and others)

## Compile & Package & Run

Before anything else you must have java.
I just searched and swing was in java since 1.6 but I developed it in 1.8 so it will run in 1.8 for sure. (if you are using 1.8< upgrade it for the god sake)

If you want the final jar file it is already in repository. otherwise you can compile and jar it as follow:
```bash
  javac SlideShow.java
  jar cfe SlideShow.jar SlideShow SlideShow*.class
```

For running  the jar file you can do:
```bash
  java -jar SlideShow.jar
```