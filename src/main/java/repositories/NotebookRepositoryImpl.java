package repositories;

import models.Notebook;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class NotebookRepositoryImpl implements NotebookRepository {

    //language=SQL
    private static final String SQL_SELECT_NOTEBOOK_BY_ID =
            "SELECT * FROM notebook WHERE id = ?";
    //language=SQL
    private static final String SQL_INSERT_NOTEBOOK =
            "INSERT INTO notebook(chat_id, surname, name, patronymic, phone, description) VALUES (?,?,?,?,?,?)";

    //language=SQL
    private static final String SQL_DELETE_NOTEBOOK =
            "DELETE FROM notebook WHERE id = ?";

    //language=SQL
    private static final String SQL_UPDATE_NOTEBOOK =
            "UPDATE notebook SET chat_id = ?, surname = ?, name = ?, patronymic = ?, phone = ?, description = ? WHERE id = ?";
    //language=SQL
    private static final String SQL_SELECT_NOTEBOOK_BY_CHAT_ID =
            "SELECT * FROM notebook WHERE chat_id = ?";
    //language=SQL
    private static final String SQL_SELECT_NOTEBOOK_BY_SNP =
            "SELECT * FROM notebook WHERE chat_id = ? AND surname = ? AND name = ? AND patronymic = ?";


    private JdbcTemplate jdbcTemplate;

    private RowMapper<Notebook> notebookRowMapper = (resultSet, i) -> Notebook.builder()
            .id(resultSet.getLong("id"))
            .chatId(resultSet.getLong("chat_id"))
            .surname(resultSet.getString("surname"))
            .name(resultSet.getString("name"))
            .patronymic(resultSet.getString("patronymic"))
            .phone(resultSet.getString("phone"))
            .description(resultSet.getString("description"))
            .build();

    public NotebookRepositoryImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Optional<Notebook> find(Long id) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(SQL_SELECT_NOTEBOOK_BY_ID, notebookRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void save(Notebook model) {
        jdbcTemplate.update(SQL_INSERT_NOTEBOOK,
                model.getChatId(),
                model.getSurname(),
                model.getName(),
                model.getPatronymic(),
                model.getPhone(),
                model.getDescription());
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update(SQL_DELETE_NOTEBOOK,id );
    }

    @Override
    public void update(Notebook model) {
        jdbcTemplate.update(SQL_UPDATE_NOTEBOOK,
                model.getChatId(),
                model.getSurname(),
                model.getName(),
                model.getPatronymic(),
                model.getPhone(),
                model.getDescription(),
                model.getId());
    }

    @Override
    public List<Notebook> findByChatId(Long chatId) {
        return jdbcTemplate.query(SQL_SELECT_NOTEBOOK_BY_CHAT_ID, notebookRowMapper, chatId);
    }

    @Override
    public Optional<Notebook> findBySNP(Long chatId, String surname, String name, String patronymic) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(SQL_SELECT_NOTEBOOK_BY_SNP, notebookRowMapper, chatId, surname, name, patronymic));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
