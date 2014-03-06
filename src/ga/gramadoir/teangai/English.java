/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ga.gramadoir.teangai;

/**
 *
 * @author ciaran
 */
public class English extends Languages{
    

    public String[] getSuggestions(String comment, String word){

        String sug;
        
        if (comment.contains("Do you mean")) {
            sug = comment.replace("Do you mean /", "").replace("/?", "").replaceAll("^\"|\"$", "");
            String[] suggestions = sug.split(", ");
            return suggestions;
        } else if (comment.contains("Unknown word: /")) {
            sug = comment.replace("Unknown word: /", "").replace("/?", "").replaceAll("^\"|\"$", "");
            String[] suggestions = sug.split(", ");
            return suggestions;
        } else if (comment.contains("You should use")) {
            sug = comment.replace("You should use /", "").replace("/ here instead", "").replaceAll("^\"|\"$", "");
            String[] suggestions = youShouldUse(sug, word);
            return suggestions;
        } else if (comment.contains("Derived form of common misspelling /")) {
            String[] suggestions = {comment.substring((comment.indexOf("/")+1), (comment.indexOf(" (")))};
            return suggestions;
        } else if (comment.contains("Derived incorrectly from the root")) {
            String[] suggestions = {comment.replace("Derived incorrectly from the root /", "").replace("/", "").replaceAll("^\"|\"$", "")};
            return suggestions;
        } else if (comment.contains("Valid word but /")) {
            String[] suggestions = {comment.replace("Valid word but /", "").replace("/ is more common", "").replaceAll("^\"|\"$", "")};
            return suggestions;
        } else if (comment.contains("Non-standard form of")) {
            sug = comment.replace("Non-standard form of /", "").replace("/", "").replaceAll("^\"|\"$", "");
            String[] suggestions = sug.split(", ");
            return suggestions;
        } else if (comment.contains("Prefix /")) {
            String prefix = comment.replace("Prefix /", "").replace("/ missing", "").replaceAll("^\"|\"$", "");
            String[] suggestions = insertPrefix(prefix, word);
            return suggestions;
        } else if (comment.contains("Unnecessary eclipsis")) {
            String[] suggestions = removeEclipsis(word);
            return suggestions;
        } else if (comment.contains("causes lenition, but this case is unclear")) {
            String[] suggestions = {insertLenition(word)};
            return suggestions;
        //} else if (comment.contains("The dependent form of the verb is required here")) {
            // NÃ­ amhain gur chonaic

        } else if (comment.contains("Unnecessary lenition")) {
            String[] suggestions = {removeLenition(word)};
            return suggestions;
        } else if (comment.contains("Lenition missing")) {
            String[] suggestions = {insertLenition(word)};
            return suggestions;
        } else if (comment.contains("Eclipsis missing")) {
            String[] suggestions = {insertEclipsis(word)};
            return suggestions;
        } else if (comment.contains("Initial mutation missing")) {
           String[] suggestions = getInitialMutation(word);
           return suggestions;
        }
           return new String[] {};
    }

    public String getErrorMessage(){
        return "Problem with Gramadóir:";
    }

}
