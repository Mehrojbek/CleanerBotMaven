package bot;
import java.sql.*;
import java.util.HashSet;

public class DbService {

    String url = "jdbc:postgresql://ec2-99-80-200-225.eu-west-1.compute.amazonaws.com/dcoknvuv7j6ebm";
    String dbUser = "vzexrdlobxmclm";
    String dbPassword = "a6c4760fcef82711865165f72078d55cbeb3a0ebd82406cc0c952ec3586cd486";


    public HashSet<Long> returnData() {
        HashSet<Long> set=new HashSet<>();
        Connection connection;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url, dbUser, dbPassword);
            Statement statement = connection.createStatement();
            String query = "select chat_id from user_of_bot;";
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

    public void newUser(Long chatId, String userName, Long members){
        Connection connection;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url, dbUser, dbPassword);
            String query = "insert into user_of_bot(chat_id,user_name,number_of_users) values(?,?,?);";
            PreparedStatement preparedStatement=connection.prepareStatement(query);
            preparedStatement.setLong(1,chatId);
            preparedStatement.setString(2,userName);
            preparedStatement.setLong(3,members);
            preparedStatement.execute();
            preparedStatement.close();
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
