import traceback
import os
import sys
import ast

#########################################################################
#                                                                       #
#   DO NOT EDIT THIS FILE !!!                                           #
#   NE MODIFIEZ PAS CE FICHIER !!!                                      #
#                                                                       #
#   Editing this file can make Pyzzle unable to work !!                 #
#   Modifier ce fichier peut rendre Pyzzle incapable de fonctioner !!   #
#                                                                       #
#########################################################################

def fatal(traceback):
    print("FATAL")
    s = traceback.format_exc()
    print(s)
    with open("FATAL_PY.txt", "w") as f:
        f.write(s)
if __name__ == "__main__":
    try:
        sys.path.append(os.path.dirname(__file__))
        import temp
        args = sys.argv[1:]

        for i in range(len(args)):
            try:
                args[i] = ast.literal_eval(args[i])
            except ValueError:
                pass
            except BaseException as e:
                fatal(traceback)
        temp.run(*args)
    except Exception as e:
        try:
            tb = e.__traceback__
            while tb.tb_next is not None:
                tb = tb.tb_next
            frame = tb.tb_frame
            print("!!!!!")
            print(type(e).__name__)
            if type(e) is SyntaxError or type(e) is IndentationError:
                print(e.lineno - 1)
            else:
                print(frame.f_lineno - 1)
                str_tb = traceback.format_tb(tb)[0]
                code = str_tb.split("\n")[-2]
                print(code.replace(" ", ""))
        except BaseException as e:
            fatal(traceback)
