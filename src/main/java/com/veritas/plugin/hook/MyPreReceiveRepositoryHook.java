package com.veritas.plugin.hook;

import com.atlassian.bitbucket.commit.CommitService;
import com.atlassian.bitbucket.content.Change;
import com.atlassian.bitbucket.content.ChangeType;
import com.atlassian.bitbucket.content.ChangesRequest;
import com.atlassian.bitbucket.hook.HookResponse;
import com.atlassian.bitbucket.hook.repository.PreReceiveRepositoryHook;
import com.atlassian.bitbucket.hook.repository.RepositoryHookContext;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.util.Page;
import com.atlassian.bitbucket.util.PageRequestImpl;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class MyPreReceiveRepositoryHook implements PreReceiveRepositoryHook {

    private static final PageRequestImpl PAGE_REQUEST = new PageRequestImpl(0, 1048576);

    @ComponentImport
    private final CommitService commitService;

    @Autowired
    public MyPreReceiveRepositoryHook(CommitService commitService) {
        this.commitService = commitService;
    }

    @Override
    public boolean onReceive(RepositoryHookContext context, Collection<RefChange> refChanges, HookResponse hookResponse) {
        final Repository repository = context.getRepository();

        for (RefChange refChange : refChanges) {

            String toHash = refChange.getToHash();
            String fromHash = refChange.getFromHash();

            ChangesRequest changesRequest = new ChangesRequest.Builder(repository, toHash).sinceId(fromHash).build();
            Page<Change> changes = commitService.getChanges(changesRequest, PAGE_REQUEST);

            for (Change change : changes.getValues()) {
                if (ChangeType.ADD.equals(change.getType())) {
                    hookResponse.out().println("You added: " + change.getPath().toString());
                }
            }
        }

        return false;
    }
}
