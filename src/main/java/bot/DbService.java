package bot;
import java.sql.*;
import java.util.HashSet;

public class DbService {
    String url = "jdbc:postgresql://ec2-54-74-156-137.eu-west-1.compute.amazonaws.com/d1jhml5tu4m04l";
    String dbUser = "jlsludtanfvbcc";
    String dbPassword = "b6cd54c858e3a5b3772c910c9826a860acb5b3ab2fc9b88a25638cc80006694c";


    public HashSet<Long> returnData() {
        HashSet<Long> set=new HashSet<>();
        Connection connection;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url, dbUser, dbPassword);
            Statement statement = connection.createStatement();
            String query = "select * from users;";
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                long chatId = resultSet.getLong("chat_id");
                set.add(chatId);
            }
            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
        return set;
    }

    public void newUser(Long chatId){
        Connection connection;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url, dbUser, dbPassword);
            Statement statement = connection.createStatement();
            String query = "insert into users(chat_id) values('"+chatId+"');";
            statement.execute(query);
            statement.close();
            connection.close();
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }

    public void deleteUser(Long chatId){
        Connection connection;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url, dbUser, dbPassword);
            Statement statement = connection.createStatement();
            String query = "delete from users where chat_id="+chatId+";";
            statement.execute(query);
            statement.close();
            connection.close();
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }
}
