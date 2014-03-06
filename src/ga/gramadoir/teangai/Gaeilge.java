/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ga.gramadoir.teangai;

/**
 *
 * @author ciaran
 */
public class Gaeilge extends Languages{

    public String[] getSuggestions(String comment, String word){

        String sug;
        
 
        if (comment.contains("An raibh /")) {
            sug = comment.replace("An raibh /", "").replace("/ ar intinn agat?", "").replaceAll("^\"|\"$", "");
            String[] suggestions = sug.split(", ");
           return suggestions;
        } else if (comment.contains("Focal anaithnid: /")) {
            sug = comment.replace("Focal anaithnid: /", "").replace("/?", "").replaceAll("^\"|\"$", "");
            String[] suggestions = sug.split(", ");
           return suggestions;
        } else if (comment.contains("Ba ch�ir duit /")) {
            sug = comment.replace("Ba ch�ir duit /", "").replace("/ a �s�id anseo", "").replaceAll("^\"|\"$", "");
            String[] suggestions = youShouldUse(sug, word);
           return suggestions;
        } else if (comment.contains("Bunaithe ar fhocal m�litrithe go coitianta /")) {
            String[] suggestions = bunaitheAr(comment.replace("Bunaithe ar fhocal m�litrithe go coitianta /", "").replace("/", "").replaceAll("^\"|\"$", ""), word);
            return suggestions;
        } else if (comment.contains("Bunaithe go m�cheart ar an bhfr�amh /")) {
            String[] suggestions = bunaitheArFreamh(comment.replace("Bunaithe go m�cheart ar an bhfr�amh /", "").replace("/", "").replaceAll("^\"|\"$", ""),word);
           return suggestions;
        } else if (comment.contains("Focal ceart ach t� /")) {
            String[] suggestions = {comment.replace("Focal ceart ach t� /", "").replace("/ n�os coitianta", "").replaceAll("^\"|\"$", "")};
           return suggestions;
        } else if (comment.contains("Foirm neamhchaighde�nach de /")) {
            sug = comment.replace("Foirm neamhchaighde�nach de /", "").replace("/", "").replaceAll("^\"|\"$", "");//.replaceAll("[\\()]","");
            String[] suggestions = sug.split(", ");
           return suggestions;
        } else if ( comment.contains("R�amhlitir /")) {
            String[] suggestions=new String[1];
            if(comment.contains("ar iarraidh")){
                String prefix = comment.replace("R�amhlitir /", "").replace("/ ar iarraidh", "").replaceAll("^\"|\"$", "");
                suggestions = insertPrefix(prefix, word);
            } else if (comment.contains("gan gh�")){
                String prefix = comment.replace("R�amhlitir /", "").replace("/ gan gh�", "").replaceAll("^\"|\"$", "");
                suggestions = removeReamhLitir(prefix, word);
            }
            return suggestions;

        } else if (comment.contains("Ur� gan gh�")) {
            String[] suggestions = removeEclipsis(word);
           return suggestions;
        } else if (comment.contains("/ go minic, ach n� l�ir � sa ch�s seo")) {
            String[] suggestions = {insertLenition(word)};
           return suggestions;
//        } else if (comment.contains("Tá gá leis an fhoirm spleách anseo")) {
            // Ní amhain gur chonaic
//           return suggestions;
        } else if (comment.contains("S�imhi� gan gh�")) {
            String[] suggestions = {removeLenition(word)};
           return suggestions;
        } else if (comment.contains("S�imhi� ar iarraidh")) {
            String[] suggestions = {insertLenition(word)};
           return suggestions;
        } else if (comment.contains("Ur� ar iarraidh")) {
            String[] suggestions = {insertEclipsis(word)};
            return suggestions;
        } else if (comment.contains("Ur� n� s�imhi� ar iarraidh")) {
            String[] suggestions =  getInitialMutation(word);
            return suggestions;
        } else if (comment.contains("N� �s�idtear an focal seo ach san abairt�n /")) {
            String s =comment.replace("N� �s�idtear an focal seo ach san abairt�n /", "").replace("/ de ghn�th", "").replaceAll("^\"|\"$", "");
            String[] suggestions  = niUsaidtear(s);
            return suggestions; 
        } else if (comment.contains("Is an fhoirm th�ite, leis an iarmh�r /-fid�s")) {
            String[] suggestions = {word.replace("feadh siad", "fid�s")};
            return suggestions;
        }

       else
            return new String[]{};


    }

    public String getErrorMessage(){
        return "Fadhb leis an Gramad�ir: ";
    }

       
}
