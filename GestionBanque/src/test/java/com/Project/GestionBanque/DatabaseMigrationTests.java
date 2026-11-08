package com.Project.GestionBanque;

import static org.assertj.core.api.Assertions.*;

import java.sql.DriverManager;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

class DatabaseMigrationTests {
  @Test
  void adoptionRequiresOptInAndPreservesExistingBalancesAndLegacyColumns() throws Exception {
    String url = "jdbc:h2:mem:legacy-adoption;DB_CLOSE_DELAY=-1";
    try (var connection = DriverManager.getConnection(url, "sa", "")) {
      ScriptUtils.executeSqlScript(connection, new ClassPathResource("legacy-h2.sql"));
      var unapproved =
          Flyway.configure()
              .dataSource(url, "sa", "")
              .locations("classpath:db/migration/h2")
              .load();
      assertThatThrownBy(unapproved::migrate).isInstanceOf(FlywayException.class);
      var approved =
          Flyway.configure()
              .dataSource(url, "sa", "")
              .locations("classpath:db/migration/h2")
              .baselineOnMigrate(true)
              .baselineVersion("0")
              .load();
      approved.migrate();
      assertThat(approved.validateWithResult().validationSuccessful).isTrue();
      assertThat(approved.info().current().getVersion().getVersion()).isEqualTo("2");
      try (var statement = connection.createStatement();
          var result =
              statement.executeQuery(
                  "SELECT solde, decouvert FROM compte_bancaire JOIN compte_courant USING(id)")) {
        assertThat(result.next()).isTrue();
        assertThat(result.getBigDecimal("solde")).isEqualByComparingTo("42.17");
        assertThat(result.getBigDecimal("decouvert")).isEqualByComparingTo("0");
      }
    }
  }
}
