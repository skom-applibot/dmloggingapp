package jp.co.applibot.sshcmd;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SshMain  {
    protected Connection sshConn;
    protected Session sshSession;

    protected String sshHostname;
    protected String sshUsername;
    protected String sshPassword;
    
    // コンストラクタ
    public SshMain(String hostname, String username, String password) {
        sshHostname=hostname;
        sshUsername=username;
        sshPassword=password;
    }

    // コマンド実行（SSHオープン⇒コマンド実行⇒SSHクローズ）
    public Boolean execCommand(String command) {
        // SSHオープン
        if(!this.open())
            return false;

        // コマンド実行
        if(!this.exec(command))
            return false;
        
        // SSHクローズ
        this.close();
        
        return true;
    }

    // SSHオープン
    protected Boolean open() {
        try
        {
            // コネクションインスタンスの作成
            sshConn = new Connection(sshHostname);

            // 接続
            sshConn.connect();

            // ユーザ／パスワード認証
            if(!sshConn.authenticateWithPassword(sshUsername, sshPassword)){
                throw new IOException("Error: Failed to authenticateWithPassword");
            }

            // SSHセッションの開始
            sshSession = sshConn.openSession();

        } catch(IOException e) {
            e.printStackTrace(System.err);
            this.close();
            return false;
        }
        
        return true;
    }

    // コマンド実行＆結果出力
    protected Boolean exec(String command) {
        try
        {
            // コマンドの実行
            sshSession.execCommand(command);
            
            // コマンド実行結果の標準出力
            this.printStdout(sshSession);

            // コマンド実行結果のエラー出力
            this.printStderr(sshSession);

        } catch(IOException e) {
            e.printStackTrace(System.err);
            this.close();
            return false;
        }
        
        return true;
    }
    
    // 実行結果の標準出力（StreamGobbler使用）
    protected void printStdout(Session sshSession) throws IOException {
        
        InputStream stdout = new StreamGobbler(sshSession.getStdout());

        BufferedReader br_out = new BufferedReader(new InputStreamReader(stdout,"UTF8"));

        while (true) {
            String line_out = br_out.readLine();
            if (line_out == null) {
                break;
            }
            System.out.println(line_out);
        }
    }

    // 実行結果のエラー出力（StreamGobbler使用）
    protected void printStderr(Session sshSession) throws IOException {
        
        InputStream stderr = new StreamGobbler(sshSession.getStderr());

        BufferedReader br_err = new BufferedReader(new InputStreamReader(stderr,"UTF8"));

        while (true) {
            String line_err = br_err.readLine();
            if (line_err == null) {
                break;
            }

            System.out.println("Error(" + sshSession.getExitStatus() + ") " + line_err);
        }
    }

    // SSHクローズ
    protected void close() {
        sshSession.close();
        sshConn.close();
    }
 }