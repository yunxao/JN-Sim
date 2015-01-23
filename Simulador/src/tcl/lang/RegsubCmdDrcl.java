// Source file: RegsubCmdDrcl.java

package tcl.lang;

import java.lang.*;

import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclIndex;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;
import tcl.lang.TclString;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class  RegsubCmdDrcl implements tcl.lang.Command 
{
    private static final int OPT_ALL= 0;
    private static final int OPT_NOCASE= 1;
    private static final int OPT_LAST= 2;
    private static final String[] validCmds= {"-all","-nocase","--"};

    public RegsubCmdDrcl()
    {
    }

    public void cmdProc(
        Interp interp, 
        tcl.lang.TclObject[] argv) throws TclException
    {
        int opt;
        String patternArg;
        String varName = null;
        Pattern pattern1;
        String tmp;
        int group;
        int nextSpecIndex;
        int first;
        int specIndex;
        int c;
        String value;
        int objc= argv.length - 1; 
        boolean noCase= false; 
        boolean all= false; 
        boolean last= false; 

        if (argv.length >= 3)
        {
            int currentObjIndex;
            for (currentObjIndex= 1;  objc > 0 && !last
				&& argv[currentObjIndex].toString().startsWith("-"); objc--) {
                opt= TclIndex.get(interp, argv[currentObjIndex], validCmds,
								"switch", 1); 
                switch (opt) {
                    case 0:
                        all= true; 
                        break;

                    case 1:
                        noCase= true; 
                        break;

                    case 2:
                        last= true; 
                        break;

                    default:
                        throw new TclException(interp,
							"RegsubCmd.cmdProc: bad option " + opt
							+ " index to cmds"); 

                }
                currentObjIndex++; 
            }

            if (objc == 3 || objc == 4)
            {
                patternArg= argv[currentObjIndex].toString(); 
                String origStringArg= argv[(currentObjIndex + 1)].toString(); 
                String stringArg= origStringArg; 
                String subSpec= argv[(currentObjIndex + 2)].toString(); 
				if (objc == 4)
                	varName= argv[(currentObjIndex + 3)].toString(); 
                if (noCase) {
                    patternArg= patternArg.toLowerCase(); 
                    stringArg= origStringArg.toLowerCase(); 
                }
                try {
                    pattern1= Pattern.compile(patternArg); 
                }
                catch (java.util.regex.PatternSyntaxException e) {
					throw new TclException(interp, e.toString());
                }
                StringBuffer sbuf= new StringBuffer(0); 
                int numMatches= 0; 
                int unmatchedIndex= 0; 
				Matcher matcher_ = pattern1.matcher(stringArg);
                while (matcher_.find())
                {
                    numMatches++; 
                    tmp= origStringArg.substring(((int)unmatchedIndex),
								matcher_.start());	
                    unmatchedIndex= matcher_.end();
                    sbuf.ensureCapacity((sbuf.length() + tmp.length())); 
                    sbuf.append(tmp); 
                    group= -1; 
                    nextSpecIndex= -1; 
                    for (first= 0; (first < subSpec.length());
									first= nextSpecIndex)
                    {
                        for (specIndex= first; (specIndex < subSpec.length());
										specIndex++)
                        {
                            c= (int) (subSpec.charAt(specIndex)); 
                            if (c == 38) {
                                group= 0; 
                                nextSpecIndex= specIndex + 1; 
                                break;
                            }
                            if (c != 92)
                                continue;
                            if ((specIndex + 1) >= subSpec.length()) {
                                continue;
                            }
                            c= (int) (subSpec.charAt((specIndex + 1))); 
                            if (Character.isDigit(((char)((char)c))) == false) {
                                sbuf.ensureCapacity(((sbuf.length()
												+ (specIndex - first)) + 1)); 
                                sbuf.append(String.valueOf(
									subSpec.substring(first, specIndex))
												+ ((char)((char)c))); 
                                first= specIndex + 2; 
                                specIndex++; 
                                continue;
                            }
                            group= Character.digit(((char)((char)c)), 10); 
                            if (group <= matcher_.groupCount()) {
                                nextSpecIndex= specIndex + 2; 
                                break;
                            }
                            sbuf.ensureCapacity((sbuf.length()
													+ (specIndex - first))); 
                            sbuf.append(subSpec.substring(first, specIndex)); 
                            first= specIndex + 2; 
                            specIndex++; 
                            continue;
                        }

                        if (specIndex >= subSpec.length())
                        {
                            sbuf.ensureCapacity((sbuf.length()
													+ (specIndex - first))); 
                            sbuf.append(subSpec.substring(first)); 
                            break;
                        }
                        c= matcher_.start(group); 
                        if (c != -1)
                        {
                            tmp= String.valueOf(
								subSpec.substring(first, specIndex))
								+ origStringArg.substring(((int)c),
												matcher_.end(group)); 
                        }
                        else
                        {
                            tmp= subSpec.substring(first, specIndex); 
                        }
                        sbuf.ensureCapacity(tmp.length()); 
                        sbuf.append(tmp); 
                    }

                    if (all == false) break;
                }

				// form replaced string
				if (numMatches == 0) value= origStringArg;
				else if (unmatchedIndex < origStringArg.length())
					value= String.valueOf(sbuf.toString())
							+ origStringArg.substring(unmatchedIndex);
				else
					value= sbuf.toString();

				if (varName == null) {
                	interp.setResult(value);
				}
				else {
                	interp.setResult(numMatches);
					try {
						interp.setVar(varName, TclString.newInstance(value), 0);
					}
					catch (TclException TclException0) {
						throw new TclException(interp,
								"couldn't set variable \"" + varName + "\"");
					}
				}
                return; 
            }
            throw new TclNumArgsException(interp, 1, argv,
							"?switches? exp string subSpec ?varName?"); 
        }
        throw new TclNumArgsException(interp, 1, argv,
					   	"?switches? exp string subSpec ?varName?"); 
    }
}

