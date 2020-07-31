package ca.jrvs.apps.grep;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jdk.nashorn.internal.runtime.Context.ThrowErrorManager;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaGrepImp implements JavaGrep {

  final Logger logger = LoggerFactory.getLogger(JavaGrep.class);

  private String regex;
  private String rootDir;
  private String outFile;

  public static void main(String[] args) {
    if (args.length != 3) {
      throw new IllegalArgumentException("USAGE: JavaGrep regex rootDir outFile");
    }

    BasicConfigurator.configure();

    JavaGrepImp javaGrepImp = new JavaGrepImp();
    javaGrepImp.setRegex(args[0]);
    javaGrepImp.setRootPath(args[1]);
    javaGrepImp.setOutFile(args[2]);

    try {
      javaGrepImp.process();
    } catch (Exception ex) {
      javaGrepImp.logger.error("Failed to start the process", ex);
    }

  }

  /**
   * Top level search workflow
   *
   * @throws IOException
   */
  @Override
  public void process() throws IOException {
    List<String> matchedLines = new ArrayList<String>();
    for (File file : listFiles(getRootPath())) {
      for (String line : readLines(file)) {
        if (containsPattern(line)) {
          matchedLines.add(line);
        }
      }
    }
    writeToFile(matchedLines);
  }

  /**
   * Traverse a given directory and return all files
   *
   * @param rootDir input Directory
   * @return files under the root directory
   */
  @Override
  public List<File> listFiles(String rootDir) {
    List<File> files = new ArrayList<File>();
    Queue<File> fileQueue = new LinkedList<>();
    fileQueue.add(new File(rootDir));
    while (!fileQueue.isEmpty()) {
      File currentFile = fileQueue.poll();
      if (currentFile.isDirectory()) {
        for (File file : currentFile.listFiles()) {
          fileQueue.add(file);
        }
      } else {
        files.add(currentFile);
      }
    }
    return files;
  }

  /**
   * Read a file and return all the lines
   *
   * @param inputFile file to be read
   * @return lines
   * @throws IllegalArgumentException if a given input file is not a file
   */
  @Override
  public List<String> readLines(File inputFile) {
    if (!inputFile.isFile()) {
      throw new IllegalArgumentException("ERROR: inputFile is not a file.");
    }
    List<String> lines = new ArrayList<>();
    try {
      BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        lines.add(line);
      }
    } catch (Exception ex) {
      logger.error("ERROR: Failed to create BufferedReader", ex);
    }
    return lines;
  }

  /**
   * check if the line contains the regex pattern (passed by the user)
   *
   * @param line input string
   * @return true if there is a match
   */
  @Override
  public boolean containsPattern(String line) {
    Pattern pattern = Pattern.compile(
        getRegex());
    Matcher matcher = pattern.matcher(line);
    boolean match = matcher.matches();
    return match;
  }

  /**
   * Write lines to a file
   *
   * @param lines matched lines
   * @throws IOException if write failed
   */
  @Override
  public void writeToFile(List<String> lines) throws IOException {
    File outputFile = new File(getOutFile());
    FileOutputStream outStream = new FileOutputStream(outputFile);
    try {
      BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outStream));
      for (String line : lines) {
        bufferedWriter.write(line);
        bufferedWriter.newLine();
      }
      bufferedWriter.flush();
      bufferedWriter.close();
    } catch (Exception ex) {
      logger.error("ERROR: The write to outFile failed", ex);
    }
  }

  @Override
  public String getRootPath() {
    return rootDir;
  }

  @Override
  public void setRootPath(String rootPath) {
    this.rootDir = rootPath;
  }

  @Override
  public String getRegex() {
    return regex;
  }

  @Override
  public void setRegex(String regex) {
    this.regex = regex;
  }

  @Override
  public String getOutFile() {
    return outFile;
  }

  @Override
  public void setOutFile(String outFile) {
    this.outFile = outFile;
  }

}
