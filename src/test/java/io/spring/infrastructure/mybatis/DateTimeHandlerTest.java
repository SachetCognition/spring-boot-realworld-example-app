package io.spring.infrastructure.mybatis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import org.apache.ibatis.type.JdbcType;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DateTimeHandlerTest {

  private DateTimeHandler handler;

  @BeforeEach
  void setUp() {
    handler = new DateTimeHandler();
  }

  @Test
  void should_set_parameter_with_datetime() throws Exception {
    PreparedStatement ps = mock(PreparedStatement.class);
    DateTime dt = new DateTime(2023, 1, 1, 0, 0, DateTimeZone.UTC);
    handler.setParameter(ps, 1, dt, JdbcType.TIMESTAMP);
    verify(ps).setTimestamp(anyInt(), any(Timestamp.class), any());
  }

  @Test
  void should_set_parameter_with_null() throws Exception {
    PreparedStatement ps = mock(PreparedStatement.class);
    handler.setParameter(ps, 1, null, JdbcType.TIMESTAMP);
    verify(ps).setTimestamp(anyInt(), isNull(), any());
  }

  @Test
  void should_get_result_by_column_name() throws Exception {
    ResultSet rs = mock(ResultSet.class);
    Timestamp ts = new Timestamp(System.currentTimeMillis());
    when(rs.getTimestamp(anyString(), any())).thenReturn(ts);
    DateTime result = handler.getResult(rs, "created_at");
    assertNotNull(result);
  }

  @Test
  void should_get_null_result_by_column_name() throws Exception {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getTimestamp(anyString(), any())).thenReturn(null);
    DateTime result = handler.getResult(rs, "created_at");
    assertNull(result);
  }

  @Test
  void should_get_result_by_column_index() throws Exception {
    ResultSet rs = mock(ResultSet.class);
    Timestamp ts = new Timestamp(System.currentTimeMillis());
    when(rs.getTimestamp(anyInt(), any())).thenReturn(ts);
    DateTime result = handler.getResult(rs, 1);
    assertNotNull(result);
  }

  @Test
  void should_get_null_result_by_column_index() throws Exception {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getTimestamp(anyInt(), any())).thenReturn(null);
    DateTime result = handler.getResult(rs, 1);
    assertNull(result);
  }

  @Test
  void should_get_result_from_callable_statement() throws Exception {
    CallableStatement cs = mock(CallableStatement.class);
    Timestamp ts = new Timestamp(System.currentTimeMillis());
    when(cs.getTimestamp(anyInt(), any())).thenReturn(ts);
    DateTime result = handler.getResult(cs, 1);
    assertNotNull(result);
  }

  @Test
  void should_get_null_result_from_callable_statement() throws Exception {
    CallableStatement cs = mock(CallableStatement.class);
    when(cs.getTimestamp(anyInt(), any())).thenReturn(null);
    DateTime result = handler.getResult(cs, 1);
    assertNull(result);
  }
}
