package com.argo.notificaciones_service.notificaciones.application;


import com.argo.notificaciones_service.notificaciones.application.dto.RequerimientoDTO;
import com.argo.notificaciones_service.notificaciones.domain.INotificaciones;
import com.argo.notificaciones_service.notificaciones.domain.Notificaciones;
import com.argo.notificaciones_service.notificaciones.domain.enm.TipoRequerimiento;
import com.argo.notificaciones_service.notificaciones.domain.enm.estadoNotificacion;
import com.argo.notificaciones_service.notificaciones.infrastructure.api.dto.mensaje;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificacionesService {


    private final String GENERAL = "/secured/user/queue/specific-user/nuevo/";
    private final String TRASLADO = "/secured/user/queue/specific-user/";
    private final INotificaciones iNotificaciones;
    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;


    @Autowired
    public NotificacionesService(INotificaciones iNotificaciones) {

        this.iNotificaciones = iNotificaciones;

    }

    @Transactional
    public void agregarNotificacionTraslado(RequerimientoDTO requerimientoDTO) {


        Notificaciones notificaciones = new Notificaciones();
        notificaciones.setIdRequerimiento(requerimientoDTO.getCodigoRequerimiento());
        notificaciones.setTipoRequerimiento(requerimientoDTO.getTipoRequerimiento());
        System.out.println(requerimientoDTO.getTipoRequerimiento());
        notificaciones.setIdAlmacen(requerimientoDTO.getAlmacenRecibe());

        Notificaciones save = this.iNotificaciones.save(notificaciones);


    }

    public List<Notificaciones> misNotificaciones(int id) {


        System.out.println("entre aca");
        return this.iNotificaciones.findByIdAlmacen(id);


    }

    @Transactional
    public void removerNotificacion(String idRequerimiento) {


        this.iNotificaciones.deleteById(idRequerimiento);
        System.out.println("borrado con exito");

    }

    @Transactional
    public void verNotificaciones(int idalmacen) {


        this.iNotificaciones.findByIdAlmacen(idalmacen).stream().forEach(notificaciones -> {

            notificaciones.setEstado(estadoNotificacion.REVISADO);
            Notificaciones save = this.iNotificaciones.save(notificaciones);

        });


    }

    private void enviarNotificacionGeneral(RequerimientoDTO idAlmacen) {

        this.agregarNotificacionTraslado(idAlmacen);
        List<Notificaciones> notificaciones = this.misNotificaciones(idAlmacen.getAlmacenRecibe());
        List<mensaje> collect = notificaciones.stream().map(notificaciones1 -> {

            mensaje m = new mensaje();
            m.setMensaje("Tiene un nuevo requerimiento de " + notificaciones1.getTipoRequerimiento().toString());
            m.setIdRequerimiento(notificaciones1.getIdRequerimiento());
            m.setEstadoNotificacion(notificaciones1.getEstado());
            m.setTipoRequerimiento(notificaciones1.getTipoRequerimiento());
            return m;
        }).collect(Collectors.toList());
        String s = String.valueOf(idAlmacen.getAlmacenRecibe());

        this.simpMessagingTemplate.convertAndSend(GENERAL + s, collect);
    }

    private void enviarNotificacionConfirmacion(RequerimientoDTO requerimiento) {
        String url = "";
        if (requerimiento.getTipoRequerimiento() == TipoRequerimiento.NECESIDAD) {
            url = TRASLADO;
            this.removerNotificacion(requerimiento.getCodigoRequerimiento());
        }
        if (requerimiento.getTipoRequerimiento() == TipoRequerimiento.NOSTOCK) {
            url = GENERAL;
            this.agregarNotificacionTraslado(requerimiento);
        }


        List<Notificaciones> notificaciones = this.misNotificaciones(requerimiento.getAlmacenRecibe());
        List<mensaje> collect = notificaciones.stream().map(notificaciones1 -> {

            mensaje m = new mensaje();
            m.setMensaje("Tiene un nuevo requerimiento de " + notificaciones1.getTipoRequerimiento().toString());
            m.setIdRequerimiento(notificaciones1.getIdRequerimiento());
            m.setEstadoNotificacion(notificaciones1.getEstado());
            m.setTipoRequerimiento(notificaciones1.getTipoRequerimiento());
            return m;
        }).collect(Collectors.toList());
        String s = String.valueOf(requerimiento.getAlmacenRecibe());
        System.out.println("este es mi 3 almacen " + s);

        this.simpMessagingTemplate.convertAndSend(url + s, collect);
    }

    private void enviarNotificacionTraslado(RequerimientoDTO requerimiento) {


        this.removerNotificacion(requerimiento.getCodigoRequerimiento());
        List<Notificaciones> notificaciones = this.misNotificaciones(requerimiento.getAlmacenRecibe());
        List<mensaje> collect = notificaciones.stream().map(notificaciones1 -> {

            mensaje m = new mensaje();
            m.setMensaje("Tiene un nuevo requerimiento de " + notificaciones1.getTipoRequerimiento().toString());
            m.setIdRequerimiento(notificaciones1.getIdRequerimiento());
            m.setEstadoNotificacion(notificaciones1.getEstado());
            m.setTipoRequerimiento(notificaciones1.getTipoRequerimiento());
            return m;
        }).collect(Collectors.toList());
        String s = String.valueOf(requerimiento.getAlmacenRecibe());
        System.out.println("este es mi 2 almacen " + s);
        this.simpMessagingTemplate.convertAndSend(TRASLADO + s, collect);

    }


    public void sendNotification(int id, RequerimientoDTO requerimientoDTO) {

        switch (id) {

            case 1:
                this.enviarNotificacionGeneral(requerimientoDTO);
                break;


            case 2:
                this.enviarNotificacionConfirmacion(requerimientoDTO);
                break;

            case 3:
            this.enviarNotificacionTraslado(requerimientoDTO);
                break;


            default:
                throw new RuntimeException("error opcion invalido");



        }

    }
}
