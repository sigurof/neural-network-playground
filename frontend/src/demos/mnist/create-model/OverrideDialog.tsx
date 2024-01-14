import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from "@mui/material";

export const OverrideDialog = ({
    open,
    onCancel,
    onContinue,
    onOverride,
}: {
    open: boolean;
    onCancel: () => void;
    onContinue: () => void;
    onOverride: () => void;
}) => {
    return (
        <Dialog open={open}>
            <DialogTitle>Override?</DialogTitle>
            <DialogContent>
                <DialogContentText>
                    A session with that ID already exists. Do you want to continue with the existing session or override
                    it?
                </DialogContentText>
            </DialogContent>
            <DialogActions>
                <Button onClick={onCancel}>Cancel</Button>
                <Button onClick={onContinue} autoFocus>
                    Continue
                </Button>
                <Button onClick={onOverride}>Override</Button>
            </DialogActions>
        </Dialog>
    );
};
