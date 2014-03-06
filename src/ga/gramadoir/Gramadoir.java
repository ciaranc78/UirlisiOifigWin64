/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ga.gramadoir;

import com.sun.star.frame.XDesktop;
import com.sun.star.lang.XComponent;
import com.sun.star.linguistic2.ProofreadingResult;
import com.sun.star.linguistic2.SingleProofreadingError;
import com.sun.star.text.XText;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import ga.gramadoir.teangai.English;
import ga.gramadoir.teangai.Gaeilge;
import ga.gramadoir.teangai.Languages;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Ciarán Campbell
 *
 */
public class Gramadoir {

    private int lengthOfPar = 0;
    private XComponentContext xContext;
    private ArrayList<String> ignoreRuleErrors = new ArrayList<String>();
    private ArrayList<String> cleanParagraphs = new ArrayList<String>();
    private ArrayList<SingleProofreadingError> errorList;
    private SingleProofreadingError error, oldError;
    private List<String> ignoreOnce = new ArrayList<String>();
    private Map<String,ArrayList<SingleProofreadingError>> storedErrors = new HashMap<String,ArrayList<SingleProofreadingError>>();
    private int offset = 0;
    private int beginOfLastError = 0;
    private int endOfLastError = 0;
    private boolean newLine = true;
    private SingleProofreadingError[] emptyError = new SingleProofreadingError[0];
    private String oldText = "";
    private String oldErrorOutput = "";
    private String lastText = "";
    private boolean paragraphChecked = false;
    private int lastStart = 0;
    private String firstErrorSentence = "";
    private SingleProofreadingError firstError;
    private SingleProofreadingError ignoreError;
    private ArrayList<SingleProofreadingError> paragraphErrors;
    private String installDir;
    private boolean newSentence = true;
    private String currentText = "";
    private boolean noMoreErrors = false;
    private ProofreadingResult prr;
    private String exe = "";
    private String executable;
    private String lang;
    private Languages language;
    private String iDir;
    private int startOfSentence=0;
    private int endOfSentence=0;
    private boolean haveErrors=false;
    private boolean isWindows=false;

    public Gramadoir(String installDir, String lang, XComponentContext xCompContext) {

        this.installDir = installDir;
        this.lang = lang;
        initGramadoir();
        executable = "\"" + installDir + "gram.exe\"";
        dummyRun();
    }

    public void initGramadoir() {
        if (lang.equals("ga")) {
            language = new Gaeilge();
        } else if (lang.equals("en")) {
            language = new English();
        } else if (lang.equals("fr")) {
            language = new English();
        }
        if(System.getProperty("os.name").startsWith("Windows"))
            isWindows=true;

        offset = 0;
        currentText = "";
        newSentence = true;
    }

    public String getEolas() {
        String perlCommand = new String(executable + " --v --comheadan=\"" + lang + "\"");
        String[] commands = {"cmd", "/c", perlCommand};
        String output = "";
        String message = "";
        String decodeStr = "";

        try {
            Process p = Runtime.getRuntime().exec(commands);
            InputStreamReader isr = new InputStreamReader(p.getInputStream());
            BufferedReader br = new BufferedReader(isr);

            while (output != null) {
                message += output + "\n";
                output = br.readLine();
            }
            try {
                decodeStr = new String(message.getBytes(), "Cp850");
            } catch (UnsupportedEncodingException uee) {
                showError(uee, output);
            }
            br.close();
            isr.close();
        } catch (Exception e) {
            showError(e, decodeStr);
        }
        return decodeStr;
    }
        private String getText() {

        //define variables
        com.sun.star.frame.XComponentLoader xCLoader;
        com.sun.star.text.XTextDocument xDoc = null;
        com.sun.star.lang.XComponent xComp = null;
        String text = "";
        try {
            // get the remote office service manager
            com.sun.star.lang.XMultiComponentFactory xMCF =
                    xContext.getServiceManager();

            Object oDesktop = xMCF.createInstanceWithContext(
                    "com.sun.star.frame.Desktop", xContext);

            XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(com.sun.star.frame.XDesktop.class, oDesktop);
            XComponent document = xDesktop.getCurrentComponent();
            XTextDocument mxDoc = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class, document);
            XText txt = mxDoc.getText();
            Object o = mxDoc.getCurrentSelection();
            String s2 = o.toString();
            text = txt.getString();

        } catch (Exception e) {
            System.err.println(" Exception " + e);
            e.printStackTrace(System.err);
        }
        return text;
    }
    public final void changeContext(final XComponentContext xCompContext) {
        xContext = xCompContext;
    }

    public final SingleProofreadingError[] getError(ProofreadingResult prr, String exe) {
        ArrayList<SingleProofreadingError> errors = new ArrayList<SingleProofreadingError>();
        String sentence = "";
        String paragraph = "";
        startOfSentence = prr.nStartOfSentencePosition;
        endOfSentence = prr.nBehindEndOfSentencePosition;
        long s = System.currentTimeMillis();
        SingleProofreadingError[] eArray;

        try {
            paragraph = prr.aText.replace("\\r\\n", "").replace("\\r", "");
            paragraph = paragraph.replaceAll("\\\n", " ");

            if ((paragraph.equals("")) || ((endOfSentence - startOfSentence) <= 1)) {
              return getEmptyError();
            } else if ((paragraph.length() <= endOfSentence) && (startOfSentence == 0)) {
                sentence = paragraph;
            } else {
                sentence = paragraph.substring(startOfSentence, endOfSentence);
            }


            sentence=sentence.replaceAll("\u2011","-");


            if (cleanParagraphs.contains(sentence)) {
                return getEmptyError();
            }
            else if (storedErrors.containsKey(sentence)) {
                ArrayList<SingleProofreadingError> myStoredErrors = storedErrors.get(sentence);
                for (SingleProofreadingError storedError : myStoredErrors){
                    if (!isIgnoreRule(storedError.aRuleIdentifier)){
                       SingleProofreadingError error = copyError(storedError);
                       errors.add(error);
                    }
                }
            }
            else {
                ArrayList<SingleProofreadingError> myStoredErrors = getErrorsFromPerl(sentence);
                for(SingleProofreadingError storedError: myStoredErrors){
                    SingleProofreadingError error = copyError(storedError);
                    errors.add(error);
                }


                Character end = sentence.charAt(sentence.length()-1);
                if(errors.isEmpty() && (!Character.isLetter(end)))
                    cleanParagraphs.add(sentence);
                else if(!Character.isLetter(end))
                    storeErrors(myStoredErrors, sentence);

            }
        }catch(Exception e){
          e.printStackTrace();
        }

        SingleProofreadingError[] errorArray = new SingleProofreadingError[errors.size()];
        errorArray=errors.toArray(errorArray);
        return errorArray;
    }

    private void dummyRun() {

        String[] commands = {"bash", "-c", executable};

        try {
            Process p = Runtime.getRuntime().exec(commands);
        } catch (Exception ioe) {
            showError(ioe, "Gramadoir Constructor");
        }

    }

    public void resetRules(){
        ignoreRuleErrors.clear();
        storedErrors.clear();
    }

    public SingleProofreadingError copyError(SingleProofreadingError originalError){
        SingleProofreadingError error = new SingleProofreadingError();
        error.aFullComment=new String(originalError.aFullComment);
        error.aShortComment=originalError.aShortComment;
        error.aSuggestions=originalError.aSuggestions;
        error.aRuleIdentifier=originalError.aRuleIdentifier;
        error.nErrorLength=originalError.nErrorLength;
        error.nErrorType=originalError.nErrorType;
        error.nErrorStart=(startOfSentence+originalError.nErrorStart);
        return error;
    }

    private void storeErrors(ArrayList<SingleProofreadingError> sentenceErrors, String sentence){
        storedErrors.put(sentence, sentenceErrors);
    }

    private Process run(String sentence){
        List<String> command = new ArrayList<String>();
        command.add("cmd");
        command.add("/c");
        command.add("echo \"" +sentence+ "\" | "+ executable +" --api --moltai --ionchod=cp850 --aschod=cp850 --comheadan=\"" + lang + "\"");
        ProcessBuilder builder = new ProcessBuilder(command);
        Map<String, String> environ = builder.environment();
        Process p = null;
        try{
            p= builder.start();
        }catch(Exception e){
           e.printStackTrace();
        }
            return p;
    }
    private Process runOnWin(String sentence){
        ArrayList<SingleProofreadingError> errors = new ArrayList<SingleProofreadingError>();
        String perlCommand = new String("echo \"" + sentence + "\"  | " + executable + " --api --moltai --ionchod=cp850 --aschod=cp850 --comheadan=\"" + lang + "\"");
        String[] commands = {"cmd", "/c", perlCommand};
        Process p=null;
        try{
          p = Runtime.getRuntime().exec(commands);
        }catch(Exception e){
           e.printStackTrace();
        }

        return p;

    }


    private ArrayList<SingleProofreadingError> getErrorsFromPerl(String sentence) {
        ArrayList<SingleProofreadingError> errors = new ArrayList<SingleProofreadingError>();
        Process p;
        if( isWindows )
            p = runOnWin(sentence);
        else
            p = run(sentence);
      
        String output="";
        InputStreamReader isr = new InputStreamReader(p.getInputStream());
        BufferedReader br = new BufferedReader(isr);
        try{
           while (output != null) {
              if (!output.startsWith("<error")) {
                 output = br.readLine();
                 continue;
             } else {
                 error = createError(output, sentence);
                  if (error == null) {
                        output = br.readLine();
                        continue;
                    }
                    errors.add(error);
                    output = br.readLine();
                }
            }
            isr.close();
            br.close();

         //   System.out.println("Run exe : "+ (System.currentTimeMillis() - s)+ " ms   "+errors.size()+"   " +sentence );

        } catch (Exception e) {
            showError(e, sentence);
        }
        return errors;

    }
    
    
    
    private SingleProofreadingError[] getEmptyError() {
        endOfLastError = 0;
        beginOfLastError = 0;
        newLine = true;
        return emptyError;
    }

    public boolean isIgnoreRule(String word) {
        if ((ignoreRuleErrors != null) && (ignoreRuleErrors.contains(word.toLowerCase()))) {
            return true;
        } else {
            return false;
        }
    }

    public void addIgnoreRule(String word) {
        ignoreRuleErrors.add(word.toLowerCase());
    }

    public boolean isIgnoreOnce(String error) {
        if (ignoreOnce.contains(error)) {
            return true;
        }
        return false;
    }

    public void addIgnoreOnce(String error) {
        ignoreOnce.add(error);
    }

    private SingleProofreadingError createError(String string, String text) {

        SingleProofreadingError error = new SingleProofreadingError();
        if(isWindows){
      
           try {
              string = new String(string.getBytes(), "Cp850");
           } catch (UnsupportedEncodingException uee) {
              showError(uee, string);
           }
        }

        Pattern p = Pattern.compile("\"([^\"]*)\"");
        Matcher m = p.matcher(string);
        int count = 0;


        int start = 0;
        int end = 0;
        while (m.find()) {
            if (count == 1) {
                start = Integer.parseInt(m.group().replaceAll("^\"|\"$", "")) - 1;
                error.nErrorStart = (start);
            } else if (count == 3) {
                end = Integer.parseInt(m.group().replaceAll("^\"|\"$", ""));
                error.nErrorLength = (end - start);
                error.aRuleIdentifier = text.substring((start), (end)).trim();
                if(isIgnoreRule(error.aRuleIdentifier))
                    return null;
            } else if (count == 5) {
                 error.aFullComment = m.group();
                error.aSuggestions = language.getSuggestions(error.aFullComment, error.aRuleIdentifier);
                error.aSuggestions = replaceUpperCase(error.aSuggestions, error.aRuleIdentifier);
                break;
//            } else if (count == 6) {
//                error.aShortComment = m.group().replaceAll("^\"|\"$", "").trim();
//                break;
            }

            count++;
        }
        error.aShortComment = error.aFullComment;
        error.nErrorType=2;
        return error;
    }

    public void showError(final Throwable e, String sen) {
        String msg = language.getErrorMessage() + sen;
        final String metaInfo = "OS: " + System.getProperty("os.name")
                + " on " + System.getProperty("os.arch") + ", Java version "
                + System.getProperty("java.vm.version")
                + " from " + System.getProperty("java.vm.vendor");
        msg += metaInfo;
        final DialogThread dt = new DialogThread(msg);
        dt.start();
    }

    public String[] replaceUpperCase(String[] suggestions, String error) {
        if(isUpperCase(error)){
             for (int i = 0; i < suggestions.length; i++) {
                suggestions[i] = suggestions[i].toUpperCase();
            }
        }
        if (Character.isUpperCase(error.charAt(0))) {
            for (int i = 0; i < suggestions.length; i++) {
                suggestions[i] = Character.toUpperCase(suggestions[i].charAt(0)) + suggestions[i].substring(1);
            }
        }
        return suggestions;
    }
    public boolean isUpperCase(String string){
       String aString = string;
       boolean upperFound = true;
       for (char c : aString.toCharArray()) {
          if ((Character.isLetter(c)) && (!Character.isUpperCase(c))) {
             upperFound = false;
             break;
         }
          upperFound=true;
       }
       return upperFound;
    }
}

class CiriSingleProofreadingError extends SingleProofreadingError implements Cloneable{
    int startSen=0;
}

