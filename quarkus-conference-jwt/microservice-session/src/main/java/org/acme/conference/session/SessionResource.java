package org.acme.conference.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.security.Authenticated;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.faulttolerance.Retry;

/**
 * SessionResource
 */
@Path("/sessions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@ApplicationScoped
public class SessionResource {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    SessionStore sessionStore;

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Fallback(fallbackMethod = "allSessionsFallback", applyOn = { Exception.class })
    @Timed(
        name = "sessionsFetch",
        description = "How long it takes to fetch all sessions.",
        unit = MetricUnits.MILLISECONDS
    )
    @Counted(
        name = "sessionsCount",
        description = "How many attendeees are interested in sessions."
    )
    @RolesAllowed("read")
    public Collection<Session> allSessions() throws Exception {
        System.out.println("WWWHHHHYYYY");
        return sessionStore.findAll();
    }

    public Collection<Session> allSessionsFallback() throws Exception {
        logger.warn("Fallback sessions");
        return sessionStore.findAllWithoutEnrichment();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("modify")
    public Session createSession(final Session session) {
        return sessionStore.save(session);
    }

    @GET
    @Path("/{sessionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Fallback(fallbackMethod = "retrieveSessionFallback")
    @CircuitBreaker(requestVolumeThreshold = 2, failureRatio = 1, delay = 30_000)
    @RolesAllowed("read")
    @Timed(
        name = "sessionDetailFetch",
        description = "How long it takes to fetch details of a session.",
        unit = MetricUnits.MILLISECONDS
    )
    @Counted(
        name = "sessionDetailCount",
        description = "How many attendeees are interested in session details."
    )
    public Response retrieveSession(@PathParam("sessionId") final String sessionId) {
        final Optional<Session> result = sessionStore.findById(sessionId);

        return result.map(s -> Response.ok(s).build()).orElseThrow(NotFoundException::new);
    }

    public Response retrieveSessionFallback(final String sessionId) {
        logger.warn("Fallback session");
        final Optional<Session> result = sessionStore.findByIdWithoutEnrichment(sessionId);
        return result.map(s -> Response.ok(s).build()).orElseThrow(NotFoundException::new);
    }

    @PUT
    @Path("/{sessionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Bulkhead(1)
    @RolesAllowed("modify")
    public Response updateSession(@PathParam("sessionId") final String sessionId, final Session session) {
        final Optional<Session> updated = sessionStore.updateById(sessionId, session);
        return updated.map(s -> Response.ok(s).build()).orElseThrow(NotFoundException::new);
    }

    @DELETE
    @Path("/{sessionId}")
    @RolesAllowed("delete")
    public Response deleteSession(@PathParam("sessionId") final String sessionId) {
        final Optional<Session> removed = sessionStore.deleteById(sessionId);
        return removed.map(s -> Response.noContent().build()).orElseThrow(NotFoundException::new);
    }

    @GET
    @Path("/{sessionId}/speakers")
    @Produces(MediaType.APPLICATION_JSON)
    @Timeout(200)
    @RolesAllowed("read")
    public Response sessionSpeakers(@PathParam("sessionId") final String sessionId) {

        final Optional<Session> session = sessionStore.findById(sessionId);
        return session.map(s -> s.speakers).map(l -> Response.ok(l).build()).orElseThrow(NotFoundException::new);
    }

    @PUT
    @Path("/{sessionId}/speakers/{speakerName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Retry(maxRetries = 60, delay = 1_000)
    @RolesAllowed("modify")
    public Response addSessionSpeaker(@PathParam("sessionId") final String sessionId,
            @PathParam("speakerName") final String speakerName) {
        final Optional<Session> result = sessionStore.findByIdWithoutEnrichment(sessionId);

        if (result.isPresent()) {
            final Session session = result.get();
            sessionStore.addSpeakerToSession(speakerName, session);
            return Response.ok(session).build();
        }

        throw new NotFoundException();
    }

    @DELETE
    @Path("/{sessionId}/speakers/{speakerName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Retry(maxRetries = 60, delay = 1_000)
    @RolesAllowed("delete")
    public Response removeSessionSpeaker(@PathParam("sessionId") final String sessionId,
            @PathParam("speakerName") final String speakerName) {
        final Optional<Session> result = sessionStore.findByIdWithoutEnrichment(sessionId);

        if (result.isPresent()) {
            final Session session = result.get();
            sessionStore.removeSpeakerFromSession(speakerName, session);
            return Response.ok(session).build();
        }

        throw new NotFoundException();
    }
}
