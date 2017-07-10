import java.util.List;
import java.util.ArrayList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Set;
import java.util.HashSet;

import java.util.Arrays;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;

public class Analyze {

	public static boolean isUpperCase2(String s)
	{
		for (int i=0; i<s.length(); i++)
		{
			if (Character.isLowerCase(s.charAt(i)))
			{
				return false;
			}
		}
		return true;
	}


	public static boolean checkExtension(String fileName, String extension) {
		boolean isAutocomment;
		String ext1 = FilenameUtils.getExtension(fileName);
		if (ext1.equals(extension)) {
			isAutocomment = true;
		} else {
			isAutocomment = false;
		}
		return isAutocomment;
	}

	public static void tfidf (
			ArrayList<MatchInstance> masterList,
			ArrayList<MatchInstance> cloneList) {

	}

	public static ArrayList<String> splitParagraph(String comment) {

		ArrayList<String> listSentences = new ArrayList<String>();

		Pattern re = Pattern.compile("[^.!?\\s][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)", Pattern.MULTILINE | Pattern.COMMENTS);
		Matcher reMatcher = re.matcher(comment);
		while (reMatcher.find()) {
			listSentences.add(reMatcher.group());
		}

		return listSentences;

	}

	public static boolean containArtifacts(String comment) {

        // search for quotations "xx"
        Pattern pattern = Pattern.compile("[\"\'](.+)[\"\']");
        Matcher matcher = pattern.matcher(comment);
        while (matcher.find()) {
			return true;
        }

        // search for xx.xx(
        pattern = Pattern.compile("\\b((([a-zA-Z_0-9]+)\\.)?([a-zA-Z_0-9]+))\\(");
        matcher = pattern.matcher(comment);
        while (matcher.find()) {
			return true;
        }

		return false;
	}

	private static Set<String> getArtifacts(String comment) {

		Set<String> artifactSet = new HashSet<String>();

		// search for quotations "xx"
		Pattern pattern = Pattern.compile("[\"\'](.+)[\"\']");
		Matcher matcher = pattern.matcher(comment);
		while (matcher.find()) {
			String result = matcher.group(1);
			artifactSet.add(result);
		}

		// search for xx.xx(
		pattern = Pattern.compile("\\b((([a-zA-Z_0-9]+)\\.)?([a-zA-Z_0-9]+))\\(");
		matcher = pattern.matcher(comment);
		while (matcher.find()) {
			String result = matcher.group(1);
			artifactSet.add(result);
		}

		// search for field names xx.xx
		pattern = Pattern.compile("\\b(([a-zA-Z_0-9]+)\\.([a-zA-Z_0-9]+))\\b");
		matcher = pattern.matcher(comment);
		while (matcher.find()) {
			String result = matcher.group(1);
			artifactSet.add(result);
		}


		// search for CamelCase
		pattern = Pattern.compile("\\b([A-Z_][a-z_0-9]*)([A-Z_][a-z_0-9]+)+\\b");
		matcher = pattern.matcher(comment);
		while (matcher.find()) {
			String result = matcher.group(0);
			artifactSet.add(result);
		}

		// search for camelCase
		pattern = Pattern.compile("\\b([a-z_][a-z_0-9]*)([A-Z_][a-z_0-9]+)+\\b");
		matcher = pattern.matcher(comment);
		while (matcher.find()) {
			String result = matcher.group(0);
			artifactSet.add(result);
		}

		// search for CAMELCASE
		pattern = Pattern.compile("\\b[A-Z0-9_]+\\b");
		matcher = pattern.matcher(comment);
		while (matcher.find()) {
			String result = matcher.group(0);
			artifactSet.add(result);
		}

		return artifactSet;

	}

	public static void codeArtifactDetection (
			HashSet<MatchInstance> masterList,
			HashSet<MatchInstance> cloneList) { 
		// search all clones's comments for code artifacts
		for (MatchInstance thisMatch : cloneList) {

			ArrayList<CommentMap> filteredCommentMap = new ArrayList<CommentMap>();

			// scan all comments
			ArrayList<CommentMap> commentList = thisMatch.getComments();
			for (CommentMap thisCommentMap : commentList) {
				Set<String> artifactSet = getArtifacts(thisCommentMap.comment);

				boolean passed = true;

				for (String thisArtifact : artifactSet) {
					// make sure all artifacts are in the code

					boolean existClone = false;
					boolean existMaster = true;

					// first check against the clones
					ArrayList<Statement> cloneStatementList = thisMatch.statements;
					for (Statement cloneStatement : cloneStatementList) {
						HashSet<String> nameList1 = cloneStatement.nameList;
						if (nameList1.contains(thisArtifact)) {
							existClone = true;
							break;
						}
					}

					// then check the master
					for (MatchInstance thisMatch2 : masterList) {
						ArrayList<Statement> masterStatementList = thisMatch2.statements;

						boolean existThisMaster = false;
						for (Statement masterStatement : masterStatementList) {
							HashSet<String> nameList2 = masterStatement.nameList;
							if (nameList2.contains(thisArtifact)) {
								existThisMaster = true;
								break;
							}
						}

						if (existThisMaster == false) {
							existMaster = false;
							break;
						}
					}

					if (!(existClone && existMaster)) {
						passed = false;
						break;
					} else {
						// capture the artifacts into the commentMap
						thisCommentMap.artifactSet = artifactSet;
					}

				}

				// add this comment to the comment map
				if (passed == true) {
					filteredCommentMap.add(thisCommentMap);
				}

			}

			thisMatch.setComments(filteredCommentMap);

		}
	} 

	public static void textSimilarity (
			HashSet<MatchInstance> masterList, 
			HashSet<MatchInstance> cloneList,
			int similarityRange,
			boolean debug,
			ArrayList<String> banListSim) {

		// get a list of global terms from all clones (master and clones)

		// master
		ArrayList<Set<String>> nameListMasterGlobal = new ArrayList<Set<String>>();
		//StanfordLemmatizer lemmatizer = new StanfordLemmatizer();
		for (MatchInstance thisMatch : masterList) {
			int startRange, endRange;
			ArrayList<Statement> statementList = thisMatch.statements;

			// variable to capture the list of unique simple names
			// full range analysis
			Set<String> simpleNameSetMasterGlobal = new HashSet<String>();
			// gather terms from the master
			startRange = 0;
			endRange = statementList.size() - 1;
			for (int i = startRange; i <= endRange; i++) {
				Statement thisStatement = statementList.get(i);
				HashSet<String> nameList = thisStatement.nameList;
				for (String str : nameList) {
					Stemmer stemmer = new Stemmer();
					for (int strIndex = 0; strIndex < str.length(); strIndex++) {
						stemmer.add(str.charAt(strIndex));
					}
					stemmer.stem();
					String stemmedStr = stemmer.toString();
					//System.out.println("dddd");
					//System.out.println(str);
					//System.out.println(stemmedStr);
					str = stemmedStr;
					//str = lemmatizer.lemmatize(str);
					simpleNameSetMasterGlobal.add(str);
				}
			}
			nameListMasterGlobal.add(simpleNameSetMasterGlobal);
			if (debug) {
				System.out.println("Master name set:");
				System.out.println(simpleNameSetMasterGlobal);
			}
		}

		// check if the comment from each clone list is valid
		ArrayList<Integer> scoreList = new ArrayList<Integer>();
		for (MatchInstance thisMatch : cloneList) {
			ArrayList<CommentMap> filteredCommentMap = new ArrayList<CommentMap>();

			// first get name list for this clone's code
			Set<String> nameListClone = new HashSet<String>();
			int startRange, endRange;
			ArrayList<Statement> statementList = thisMatch.statements;
			// variable to capture the list of unique simple names
			// full range analysis
			// gather terms from the master
			startRange = 0;
			endRange = statementList.size() - 1;
			for (int i = startRange; i <= endRange; i++) {
				Statement thisStatement = statementList.get(i);
				HashSet<String> nameList = thisStatement.nameList;
				for (String str : nameList) {
					nameListClone.add(str);
				}
			}
			if (debug) {
				System.out.println("Clone list terms:");
				System.out.println(nameListClone);
			}

			// second get name list for each comment
			ArrayList<Set<String>> nameListComment = new ArrayList<Set<String>>();
			ArrayList<CommentMap> commentList = thisMatch.commentList;
			for (CommentMap cMap : commentList) {
				// get a list of terms from sentence
				String thisComment = cMap.comment;
				Set<String> sentenceTermList = Utilities.extractTermsFromSentence(thisComment, banListSim, debug);
				if (debug) {
					System.out.println("Comment: \"" + thisComment + "\"");
					System.out.println("List of terms from comment:");
					System.out.println(sentenceTermList);
				}
				nameListComment.add(sentenceTermList);
			}

			// find list of terms between this clone and comment,
			// which will have to exist in master or else discard
			ArrayList<HashSet<String>> setMustExistMaster = new ArrayList<HashSet<String>>();
			// loop over each comment
			for (Set<String> comment : nameListComment) {
				HashSet<String> commentSetMustExist = new HashSet<String>();
				// loop over each term in comment
				for (String commentL : comment) {
					// loop on clone terms
					for (String cloneL : nameListClone) {
						if (cloneL.toLowerCase().contains(commentL)) {
							commentSetMustExist.add(commentL);
						}
					}
				}
				setMustExistMaster.add(commentSetMustExist);
			}
			if (debug) {
				System.out.println("Must exist in master:");
				System.out.println(setMustExistMaster);
			}

			// check if it exist in every master
			int commentIndex = 0;
			for (Set<String> commentTermSet : setMustExistMaster) {
				Set<String> failedTerms = new HashSet<String>();
				boolean failed = false;
				for (Set<String> masterSet : nameListMasterGlobal) {
					for (String str1 : commentTermSet) {
						boolean thisMasterStatus = false;
						for (String str2 : masterSet) {
							if (!str2.toLowerCase().contains(str1)) {
								failedTerms.add(str1);
							} else {
								thisMasterStatus = true;
								break;
							}
						}
						if (thisMasterStatus == false) {
							failed = true;
						}
					}
				}

				int score = commentTermSet.size();
				if (!failed && score > 0) {
					CommentMap thisMap = thisMatch.commentList.get(commentIndex);
					filteredCommentMap.add(thisMap);
					scoreList.add(new Integer(score));
				}
				if (debug) {
					if (failed) {
						System.out.println("Failed to locate all the needed terms in master:");
						System.out.println(failedTerms);
					} else {
						System.out.println("Found all the needed terms in master");
					}
				}

				commentIndex = commentIndex + 1;
			}

			thisMatch.commentList = filteredCommentMap;
			thisMatch.scoreList = scoreList;
			thisMatch.setMustExistMaster = setMustExistMaster;
		}

	}

	public static boolean containInvalidTerms(String comment, boolean debug) {
		if (comment.contains("?") || comment.contains("http") || comment.contains("https") ||
				comment.contains(";") || comment.contains("$NON-NLS-1$") || 
				comment.contains("{@inheritDoc}")) {
			if (debug) {
				System.out.println("Removed due to invalid term");
			}
			return true;
		}

		boolean isBad = containsBadTerm(comment);
		if (isBad) {
			if (debug) {
				System.out.println("Removed due to banned term");
			}
			return true;
		}

		return false;
	}

	static boolean containsBadTerm(String comment) {

		String stopWordPattern = "";
		String line;
		try {
			BufferedReader br = new BufferedReader (
					new FileReader("./negative.txt"));
			while ((line = br.readLine()) != null) {
				stopWordPattern += "\\b" + line + "\\b|";
			}
			stopWordPattern = stopWordPattern.substring(0,stopWordPattern.length()-1);
		} catch (IOException e) {
			System.out.println("Error while reading stopword file" + e);
			System.exit(0);
		}

		// Compile the pattern
		Pattern p = Pattern.compile("(" + stopWordPattern + ")");
		Matcher m = p.matcher(comment.toLowerCase());

		if (m.find()) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean hasValidScope (List<Statement> statementList) {
		int baseLineScope = statementList.get(0).scopeLevel;
		for (int i = 1; i < statementList.size(); i++) {
			if (statementList.get(i).scopeLevel < baseLineScope) {
				// scope level should not go backwards,
				// only equal or larger
				return false;
			}
		}
		return true;
	}

	public static boolean hasHashError (List<Statement> statementList1, List<Statement> statementList2) {
		for (int i = 0; i < statementList1.size(); i++) {
			if (statementList1.get(i).hashNumber != statementList2.get(i).hashNumber) {
				return true;
			}
		}

		return false;
	}

	public static boolean isRepetitive(List<Statement> statementList) {
		boolean isRepetitive = false;
		int baseline = statementList.get(0).hashNumber;
		for (int i = 1; i < statementList.size(); i++) {
			int currentHashNumber = statementList.get(i).hashNumber;
			if (currentHashNumber == baseline) {
				isRepetitive = true;
				break;
			} else {
				baseline = currentHashNumber;
			}
		}

		if (isRepetitive) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean checkExistNumbers (String comment, boolean debug) {
		String pattern = "[0-9]+";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(comment);
		if (m.find()) {
			if (debug) {
				System.out.println("Removed because it has a number");
			}
			return true;
		} else {
			if (debug) {
				System.out.println("Not removed from number check");
			}
			return false;
		}
	}

	// return true if satisfied
	public static boolean checkNumberTermsIsGood (String comment, int minNumTerms, boolean debug) {

		String[] words = comment.split("\\s+");
		if (words.length >= minNumTerms) {
			if (debug) {
				System.out.println("Satisfied number of minimum terms in comment");
			}
			return true;
		} else {
			if (debug) {
				System.out.println("Not satisfied number of minimum terms in comment:");
				System.out.println("only found " + words.length + " terms");
				System.out.println(comment);
			}
			return false;
		}
	}

	public static boolean checkNumMethods(List<Statement> statementList, int minThreshold) {

		int numMethodStatements = 0;
		for (int i = 0; i < statementList.size(); i++) {
			if (statementList.get(i).hasMethodInvocation() == true) {
				numMethodStatements++;
			}
		}

		if (numMethodStatements >= minThreshold) {
			return true;
		} else {
			return false;
		}

	}

}

