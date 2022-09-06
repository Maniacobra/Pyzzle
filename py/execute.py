import traceback
import sys

if __name__ == "__main__":
    try:
        import temp
        args = sys.argv[1:]
        for i in range(len(args)):
            if args[i].isnumeric():
                args[i] = int(args[i])
            elif args[i] == "True":
                args[i] = True
            elif args[i] == "False":
                args[i] = False
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
            print("FATAL")
            print(traceback.format_exc())