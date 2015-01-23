// Source file: RegexpCmdDrcl.java

package tcl.lang;

import java.lang.*;

import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclIndex;
import tcl.lang.TclInteger;
import tcl.lang.TclList;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;
import tcl.lang.TclString;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class  RegexpCmdDrcl implements tcl.lang.Command 
{
    private static final int OPT_INDICES= 0;
    private static final int OPT_NOCASE= 1;
    private static final int OPT_LAST= 2;
    private static final String[] validCmds = {"-indices","-nocase","--"};

    public RegexpCmdDrcl()
    {}

    public void cmdProc(Interp interp, tcl.lang.TclObject[] argv)
		throws TclException
    {
        int opt;
        String patternArg;
        Pattern pattern1;
        int objc= argv.length - 1; 
        boolean noCase= false; 
        boolean indices= false; 
        boolean last= false; 
        if (argv.length >= 3) {
            int currentObjIndex;
            for (currentObjIndex= 1;  objc > 0 && !last
				&& argv[currentObjIndex].toString().startsWith("-");
				currentObjIndex++) {
                opt= TclIndex.get(interp, argv[currentObjIndex], validCmds,
								"switch", 1); 
                switch (opt)
                {
                    case 0:
                        indices= true; 
                        break;

                    case 1:
                        noCase= true; 
                        break;

                    case 2:
                        last= true; 
                        break;

                    default:
                        throw new TclException(interp,
							"RegexpCmd.cmdProc: bad option " + opt
							+ " index to validCmds"); 
                }
                objc--; 
            }

            if (objc >= 2)
            {
                patternArg= argv[currentObjIndex].toString(); 
                String origStringArg= argv[(currentObjIndex + 1)].toString(); 
                String stringArg= origStringArg; 
                currentObjIndex += 2; 
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
				Matcher matcher_ = pattern1.matcher(stringArg);
                if (!matcher_.find()) {
                    interp.setResult(0); 
                }
                else
                {
                    interp.setResult(1); 
                    int g;
                    for (g= 0; (objc > 2); currentObjIndex++) {
                        if (g > matcher_.groupCount())
                            break;
						if (indices) {
							int start = matcher_.start(g);
							int end = matcher_.end(g);

							setMatchVar(interp,
									argv[currentObjIndex].toString(),
									start, (end - 1)); 
						}
						else {
							setMatchStringVar(interp,
									argv[currentObjIndex].toString(),
									matcher_.group(g)); 
						}

                        g++; 
                        objc--; 
                    }

                }
                while (objc > 2) {
                    if (indices == false)
                        setMatchStringVar(interp,
								argv[currentObjIndex].toString(), ""); 
                    else
                        setMatchVar(interp, argv[currentObjIndex].toString(),
										-1, -1); 
                    objc--; 
                    currentObjIndex++; 
                    continue;
                }

                return; 
            }
            throw new TclNumArgsException(interp, 1, argv,
			"?switches? exp string ?matchVar? ?subMatchVar subMatchVar ...?"); 
        }
        throw new TclNumArgsException(interp, 1, argv,
			"?switches? exp string ?matchVar? ?subMatchVar subMatchVar ...?"); 
    }

    private static void setMatchVar(
        Interp interp, 
        String varName, 
        int start, 
        int end) throws TclException
    {
        try {
            TclObject indexPairObj= TclList.newInstance(); 
            TclList.append(interp, indexPairObj, TclInteger.newInstance(start)); 
            TclList.append(interp, indexPairObj, TclInteger.newInstance(end)); 
            interp.setVar(varName, indexPairObj, 0); 
        }
        catch (TclException TclException0) {
            throw new TclException(interp, "couldn't set variable \""
							+ varName + "\""); 
        }
    }


    private static void setMatchStringVar(
        Interp interp, 
        String varName, 
        String valueString) throws TclException
    {
        TclObject valueObj = TclString.newInstance(valueString == null?
						"": valueString); 
        try
        {
            interp.setVar(varName, valueObj, 0); 
        }
        catch (TclException TclException0)
        {
            throw new TclException(interp, "couldn't set variable \"" + varName + "\""); 
        }
    }
}

