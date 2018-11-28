package com.inovaufrpe.makeparty.fornecedor.gui;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.inovaufrpe.makeparty.R;
import com.inovaufrpe.makeparty.fornecedor.dominio.Agendamento;
import com.inovaufrpe.makeparty.infra.utils.AgendamentoBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {
    private CompactCalendarView calendarView;
    private SimpleDateFormat sdf = new SimpleDateFormat("MMMM/yyyy");
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        configurarTela();
    }

    private void configurarTela(){
        calendarView = findViewById(R.id.compactcalendar_view) ;

        textView = findViewById(R.id.mes);
        textView.setText(sdf.format(calendarView.getFirstDayOfCurrentMonth()));

        calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                Intent intent = new Intent(CalendarActivity.this, CapturaDadosCalendarActivity.class);
                intent.putExtra("dataLongMiliseconds", dateClicked.getTime());
                startActivity(intent);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                textView.setText(sdf.format(calendarView.getFirstDayOfCurrentMonth()));
            }
        });
        configurarEventos();
    }

    private void configurarEventos(){
        List<Agendamento> list = AgendamentoBuilder.gerarAgendamentos();
        List<Event> events = new ArrayList<>();
        for (Agendamento agendamento: list){
            events.add(new Event(Color.parseColor("#006400"), agendamento.getDateInicio().getTime()));
        }
        calendarView.addEvents(events);
    }
}



